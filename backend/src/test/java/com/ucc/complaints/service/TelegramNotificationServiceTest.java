package com.ucc.complaints.service;

import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.TelegramLink;
import com.ucc.complaints.repository.ComplaintRepository;
import com.ucc.complaints.repository.TelegramLinkRepository;
import com.ucc.complaints.telegram.TelegramClient;
import com.ucc.complaints.telegram.TelegramCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTest {
    @Mock
    private TelegramLinkRepository links;

    @Mock
    private TelegramClient client;

    @Mock
    private ComplaintRepository complaints;

    private TelegramCryptoService cryptoService;
    private TelegramNotificationService service;

    @BeforeEach
    void setUp() {
        cryptoService = new TelegramCryptoService("test-key");
        service = new TelegramNotificationService(links, client);
    }

    @Test
    void notifiesWhenActiveAndEnabled() {
        TelegramLink link = new TelegramLink("hash-123", OffsetDateTime.now());
        link.setNotificationsEnabled(true);
        when(links.findFirstByActiveTrue()).thenReturn(Optional.of(link));

        service.notifyNewComplaint("C-1", "Infraestructura");

        verify(client).sendMessage(eq("hash-123"), contains("Infraestructura"));
    }

    @Test
    void usesDecryptedChatIdAndIncludesNonSensitiveComplaintMetadata() {
        TelegramNotificationService fullService = new TelegramNotificationService(links, client, cryptoService);
        fullService.setComplaintRepository(complaints);

        TelegramLink link = new TelegramLink("hash-123", OffsetDateTime.now());
        link.setEncryptedChatId(cryptoService.encrypt("99887766"));
        link.setNotificationsEnabled(true);
        when(links.findFirstByActiveTrue()).thenReturn(Optional.of(link));

        Complaint complaint = new Complaint("C-99", false, "Laboratorio",
            "Asunto confidencial no enviar", "Descripcion confidencial", OffsetDateTime.now());
        complaint.setLocationNumber("Lab-302");
        complaint.setName("Estudiante Secreto");
        when(complaints.findByPublicId("C-99")).thenReturn(Optional.of(complaint));

        fullService.notifyNewComplaint("C-99", "Laboratorio");

        verify(client).sendMessage(eq("99887766"), argThat(msg ->
            msg.contains("C-99")
                && msg.contains("Laboratorio")
                && msg.contains("Lab-302")
                && msg.contains("Identificada")
                && !msg.contains("Asunto confidencial")
                && !msg.contains("Estudiante Secreto")));
    }

    @Test
    void doesNotNotifyWhenDisabled() {
        TelegramLink link = new TelegramLink("hash-123", OffsetDateTime.now());
        link.setNotificationsEnabled(false);
        when(links.findFirstByActiveTrue()).thenReturn(Optional.of(link));

        service.notifyNewComplaint("C-1", "Infraestructura");

        verifyNoInteractions(client);
    }

    @Test
    void handlesClientExceptionGracefully() {
        TelegramLink link = new TelegramLink("hash-123", OffsetDateTime.now());
        link.setNotificationsEnabled(true);
        when(links.findFirstByActiveTrue()).thenReturn(Optional.of(link));
        doThrow(new RuntimeException("Telegram unreachable")).when(client).sendMessage(any(), any());

        // Debe capturar la excepción y no propagarla
        service.notifyNewComplaint("C-1", "Infraestructura");
    }
}
