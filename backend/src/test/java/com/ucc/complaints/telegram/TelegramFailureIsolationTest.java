package com.ucc.complaints.telegram;

import com.ucc.complaints.repository.TelegramLinkRepository;
import com.ucc.complaints.service.TelegramNotificationService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TelegramFailureIsolationTest {
    @Test
    void telegramFailureDoesNotEscalate() {
        TelegramLinkRepository links = mock(TelegramLinkRepository.class);
        TelegramClient client = mock(TelegramClient.class);
        doThrow(new TelegramDeliveryException("offline", null)).when(client).sendMessage(anyString(), anyString());
        TelegramNotificationService service = new TelegramNotificationService(links, client);
        when(links.findFirstByActiveTrue()).thenReturn(java.util.Optional.empty());
        assertDoesNotThrow(() -> service.notifyNewComplaint("public-id", "Laboratorio"));
    }
}
