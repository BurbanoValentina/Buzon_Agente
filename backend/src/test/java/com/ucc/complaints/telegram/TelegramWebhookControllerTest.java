package com.ucc.complaints.telegram;

import com.ucc.complaints.controller.TelegramWebhookController;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TelegramWebhookControllerTest {
    @Test
    void rejectsInvalidWebhookSecret() {
        TelegramClient client = new TelegramClient() {
            public void sendMessage(String chatId, String message) {}
            public boolean acceptsWebhookSecret(String secret) { return false; }
            public java.util.Map<String, Object> buildUpdate(String text, String accountId) { return java.util.Map.of(); }
        };
        TelegramWebhookController controller = new TelegramWebhookController(client, null, null);
        assertEquals(401, controller.webhook("invalid", java.util.Map.of()).getStatusCode().value());
    }
}
