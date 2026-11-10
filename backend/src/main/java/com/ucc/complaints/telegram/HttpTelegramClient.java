package com.ucc.complaints.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HttpTelegramClient implements TelegramClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpTelegramClient.class);
    private static final String TELEGRAM_API_BASE = "https://api.telegram.org";
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
        new ParameterizedTypeReference<>() {};

    private final String botToken;
    private final String webhookSecret;
    private final boolean enabled;
    private final RestClient restClient;
    private volatile Map<String, Object> cachedBotInfo;

    @Autowired
    public HttpTelegramClient(
        @Value("${telegram.bot-token:}") String botToken,
        @Value("${telegram.webhook-secret:}") String webhookSecret,
        @Value("${telegram.enabled:false}") boolean enabled) {
        this(botToken, webhookSecret, enabled, createDefaultClient());
    }

    HttpTelegramClient(String botToken, String webhookSecret, boolean enabled, RestClient restClient) {
        this.botToken = botToken == null ? "" : botToken.trim();
        this.webhookSecret = webhookSecret == null ? "" : webhookSecret.trim();
        this.enabled = enabled;
        this.restClient = restClient;
    }

    private static RestClient createDefaultClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        return RestClient.builder()
            .baseUrl(TELEGRAM_API_BASE)
            .requestFactory(factory)
            .build();
    }

    @Override
    public void sendMessage(String chatId, String message) {
        if (!enabled || botToken.isBlank()) {
            return;
        }
        try {
            restClient.post()
                .uri("/bot" + botToken + "/sendMessage")
                .body(Map.of("chat_id", chatId, "text", message))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            LOGGER.warn("Telegram sendMessage rejected with HTTP {}", exception.getStatusCode().value());
            throw new TelegramDeliveryException("No fue posible entregar el aviso por Telegram.", exception);
        } catch (RuntimeException exception) {
            LOGGER.warn("Telegram sendMessage transport failure: {}", exception.getClass().getSimpleName());
            throw new TelegramDeliveryException("No fue posible entregar el aviso por Telegram.", exception);
        }
    }

    @Override
    public boolean acceptsWebhookSecret(String secret) {
        return enabled && !webhookSecret.isBlank() && webhookSecret.equals(secret);
    }

    @Override
    public Map<String, Object> buildUpdate(String messageText, String accountId) {
        return Map.of("text", messageText == null ? "" : messageText, "accountId", accountId == null ? "" : accountId);
    }

    @Override
    public Map<String, Object> getBotInfo() {
        if (!enabled || botToken.isBlank()) {
            return Map.of("enabled", false, "username", "", "firstName", "");
        }
        if (cachedBotInfo != null) {
            return cachedBotInfo;
        }
        try {
            Map<String, Object> response = restClient.get()
                .uri("/bot" + botToken + "/getMe")
                .retrieve()
                .body(MAP_TYPE);
            if (response != null && Boolean.TRUE.equals(response.get("ok"))
                && response.get("result") instanceof Map<?, ?> result) {
                String username = result.get("username") instanceof String value ? value : "";
                String firstName = result.get("first_name") instanceof String value ? value : "Bot UCC";
                Map<String, Object> info = Map.of(
                    "enabled", true,
                    "username", username,
                    "firstName", firstName
                );
                cachedBotInfo = info;
                return info;
            }
        } catch (RuntimeException ignored) {
            // Fall back safely without exposing tokens or internal traces
        }
        return Map.of("enabled", true, "username", "", "firstName", "Buzón UCC Bot");
    }

    @Override
    public List<Map<String, Object>> fetchUpdates(long offset, int timeoutSeconds) {
        if (!enabled || botToken.isBlank()) {
            return List.of();
        }
        try {
            Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/bot" + botToken + "/getUpdates")
                    .queryParam("offset", offset)
                    .queryParam("timeout", timeoutSeconds)
                    .build())
                .retrieve()
                .body(MAP_TYPE);
            if (response != null && Boolean.TRUE.equals(response.get("ok"))
                && response.get("result") instanceof List<?> rawList) {
                List<Map<String, Object>> updates = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> rawMap) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> typed = (Map<String, Object>) rawMap;
                        updates.add(typed);
                    }
                }
                return updates;
            }
        } catch (RuntimeException exception) {
            throw new TelegramDeliveryException("No fue posible consultar actualizaciones de Telegram.", exception);
        }
        return List.of();
    }

    @Override
    public void clearWebhookForPolling() {
        if (!enabled || botToken.isBlank()) {
            return;
        }
        try {
            restClient.post()
                .uri("/bot" + botToken + "/deleteWebhook")
                .body(Map.of("drop_pending_updates", false))
                .retrieve()
                .toBodilessEntity();
        } catch (RuntimeException ignored) {
            // Polling loop handles temporary network downtime cleanly
        }
    }
}
