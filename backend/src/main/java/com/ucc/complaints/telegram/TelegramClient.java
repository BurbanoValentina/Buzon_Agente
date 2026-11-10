package com.ucc.complaints.telegram;

import java.util.List;
import java.util.Map;

public interface TelegramClient {
    void sendMessage(String chatId, String message);
    boolean acceptsWebhookSecret(String secret);
    Map<String, Object> buildUpdate(String messageText, String accountId);

    default Map<String, Object> getBotInfo() {
        return Map.of("enabled", false, "username", "", "firstName", "");
    }

    default List<Map<String, Object>> fetchUpdates(long offset, int timeoutSeconds) {
        return List.of();
    }

    default void clearWebhookForPolling() {
        // Optional lifecycle hook before long polling starts
    }
}
