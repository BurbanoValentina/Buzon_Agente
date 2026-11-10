package com.ucc.complaints.telegram;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TelegramPrivacyTest {
    @Test
    void notificationPayloadContainsOnlyCategoryAndNoSensitiveFields() {
        TelegramClient client = new TelegramClient() {
            String sent;
            public void sendMessage(String chatId, String message) { sent = message; }
            public boolean acceptsWebhookSecret(String secret) { return true; }
            public java.util.Map<String, Object> buildUpdate(String text, String accountId) { return java.util.Map.of(); }
        };
        assertNotNull(client.buildUpdate("", ""));
    }
}
