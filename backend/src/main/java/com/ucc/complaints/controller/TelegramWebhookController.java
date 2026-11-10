package com.ucc.complaints.controller;

import com.ucc.complaints.service.TelegramCommandService;
import com.ucc.complaints.service.TelegramLinkService;
import com.ucc.complaints.telegram.TelegramChallengeService;
import com.ucc.complaints.telegram.TelegramClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/telegram")
public class TelegramWebhookController {
    private final TelegramClient client;
    private final TelegramCommandService commandService;
    private final TelegramLinkService linkService;
    private TelegramChallengeService challengeService = new TelegramChallengeService();

    public TelegramWebhookController(TelegramClient client, TelegramCommandService commandService,
                                     TelegramLinkService linkService) {
        this.client = client;
        this.commandService = commandService;
        this.linkService = linkService;
    }

    @Autowired(required = false)
    public void setChallengeService(TelegramChallengeService challengeService) {
        if (challengeService != null) {
            this.challengeService = challengeService;
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
        @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secret,
        @RequestBody Map<String, Object> update) {
        if (!client.acceptsWebhookSecret(secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<?, ?> message = update.get("message") instanceof Map<?, ?> map ? map : Map.of();
        String text = message.get("text") instanceof String value ? value : "";
        String accountId = extractId(message.get("chat"));
        if (accountId.isBlank()) {
            accountId = extractId(message.get("from"));
        }
        if (text.trim().toLowerCase(Locale.ROOT).startsWith("/start") && !accountId.isBlank()) {
            client.sendMessage(accountId,
                "Bot Oficial · Buzón UCC\nTu ID numérico de chat es: " + accountId);
            return ResponseEntity.ok().build();
        }
        String accountHash = accountId.isBlank() ? "" : challengeService.hash(accountId);
        if (linkService.status().linked()) {
            String response = commandService.execute(accountHash, text);
            if (!response.isBlank()) {
                client.sendMessage(accountId, response);
            }
        }
        return ResponseEntity.ok().build();
    }

    private String extractId(Object container) {
        if (container instanceof Map<?, ?> map && map.get("id") != null) {
            return String.valueOf(map.get("id"));
        }
        return "";
    }
}
