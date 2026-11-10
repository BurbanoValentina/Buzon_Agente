package com.ucc.complaints.telegram;

import com.ucc.complaints.service.TelegramCommandService;
import com.ucc.complaints.service.TelegramLinkService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class TelegramLongPollingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramLongPollingService.class);

    private final TelegramClient telegramClient;
    private final TelegramCommandService commandService;
    private final TelegramLinkService linkService;
    private final TelegramChallengeService challengeService;
    private final boolean enabled;
    private final boolean longPolling;
    private final String botToken;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread workerThread;
    private long nextOffset = 0L;

    public TelegramLongPollingService(
        TelegramClient telegramClient,
        TelegramCommandService commandService,
        TelegramLinkService linkService,
        TelegramChallengeService challengeService,
        @Value("${telegram.enabled:false}") boolean enabled,
        @Value("${telegram.long-polling:false}") boolean longPolling,
        @Value("${telegram.bot-token:}") String botToken) {
        this.telegramClient = telegramClient;
        this.commandService = commandService;
        this.linkService = linkService;
        this.challengeService = challengeService;
        this.enabled = enabled;
        this.longPolling = longPolling;
        this.botToken = botToken == null ? "" : botToken.trim();
    }

    @PostConstruct
    public void start() {
        if (!enabled || !longPolling || botToken.isBlank()) {
            return;
        }
        if (running.compareAndSet(false, true)) {
            workerThread = new Thread(this::pollLoop, "telegram-long-polling");
            workerThread.setDaemon(true);
            workerThread.start();
        }
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    private void pollLoop() {
        telegramClient.clearWebhookForPolling();
        Map<String, Object> botInfo = telegramClient.getBotInfo();
        String botUsername = String.valueOf(botInfo.getOrDefault("username", ""));
        LOGGER.info("Telegram long polling started (botUsernamePresent={})", !botUsername.isBlank());

        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                pollOnce();
            } catch (RuntimeException exception) {
                LOGGER.warn("Telegram polling iteration failed safely: {}", exception.getMessage());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public void pollOnce() {
        List<Map<String, Object>> updates = telegramClient.fetchUpdates(nextOffset, 20);
        for (Map<String, Object> update : updates) {
            long updateId = extractLong(update.get("update_id"));
            if (updateId >= nextOffset) {
                nextOffset = updateId + 1;
            }
            processUpdate(update);
        }
    }

    public void processUpdate(Map<String, Object> update) {
        if (update == null || update.isEmpty()) {
            return;
        }
        Object rawMessage = update.containsKey("message") ? update.get("message") : update.get("edited_message");
        if (!(rawMessage instanceof Map<?, ?> messageMap)) {
            return;
        }
        String text = messageMap.get("text") instanceof String value ? value.trim() : "";
        if (text.isBlank()) {
            return;
        }
        String chatId = extractId(messageMap.get("chat"));
        if (chatId.isBlank()) {
            chatId = extractId(messageMap.get("from"));
        }
        if (chatId.isBlank()) {
            return;
        }

        String accountHash = challengeService.hash(chatId);
        String normalized = text.toLowerCase(Locale.ROOT);
        if ("/start".equals(normalized) || normalized.startsWith("/start ")) {
            boolean isLinkedHere = linkService.activeAccountHash()
                .map(active -> active.equals(accountHash))
                .orElse(false);
            String welcome = isLinkedHere
                ? "Tu cuenta ya está vinculada al Buzón UCC.\n\n"
                    + "Comandos disponibles (solo lectura):\n"
                    + "• Ayuda\n• Resumen\n• Pendientes\n• Mes"
                : "Bot Oficial · Buzón UCC\n\n"
                    + "Tu ID numérico de chat es:\n" + chatId + "\n\n"
                    + "Pega este número en el panel administrativo (pestaña Bot Telegram) para recibir tu código de vinculación de 10 minutos.";
            sendSafely(chatId, welcome);
            return;
        }

        String reply = commandService.execute(accountHash, text);
        if (reply != null && !reply.isBlank()) {
            sendSafely(chatId, reply);
        }
    }

    private void sendSafely(String chatId, String text) {
        try {
            telegramClient.sendMessage(chatId, text);
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to send Telegram reply safely.");
        }
    }

    private String extractId(Object container) {
        if (container instanceof Map<?, ?> map && map.get("id") != null) {
            return String.valueOf(map.get("id"));
        }
        return "";
    }

    private long extractLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str) {
            try {
                return Long.parseLong(str.trim());
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }
}
