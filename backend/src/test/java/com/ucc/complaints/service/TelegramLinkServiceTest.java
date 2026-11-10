package com.ucc.complaints.service;

import com.ucc.complaints.model.TelegramLink;
import com.ucc.complaints.repository.TelegramLinkRepository;
import com.ucc.complaints.telegram.TelegramCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramLinkServiceTest {
    @Mock
    private TelegramLinkRepository repository;

    @Mock
    private AuditService auditService;

    private TelegramLinkService service;

    @BeforeEach
    void setUp() {
        service = new TelegramLinkService(repository, auditService, new TelegramCryptoService("secret"));
    }

    @Test
    void returnsUnlinkedWhenNoActiveLink() {
        when(repository.findFirstByActiveTrue()).thenReturn(Optional.empty());

        TelegramLinkService.TelegramLinkStatus status = service.status();

        assertFalse(status.linked());
        assertFalse(status.notificationsEnabled());
        assertTrue(service.resolveActiveChatId().isEmpty());
        assertTrue(service.activeAccountHash().isEmpty());
    }

    @Test
    void linksNewAccountWithEncryptedChatIdAndDeactivatesPrevious() {
        TelegramLink previous = new TelegramLink("prev-hash", OffsetDateTime.now());
        when(repository.findFirstByActiveTrue()).thenReturn(Optional.of(previous));
        when(repository.findByTelegramAccountHash("new-hash")).thenReturn(Optional.empty());

        service.link("new-hash", "44556677");

        assertFalse(previous.isActive());
        assertNotNull(previous.getUnlinkedAt());
        verify(repository).save(previous);

        ArgumentCaptor<TelegramLink> captor = ArgumentCaptor.forClass(TelegramLink.class);
        verify(repository, times(2)).save(captor.capture());
        TelegramLink savedNew = captor.getAllValues().get(1);
        assertEquals("new-hash", savedNew.getTelegramAccountHash());
        assertNotNull(savedNew.getEncryptedChatId());
        assertNotEquals("44556677", savedNew.getEncryptedChatId());
        verify(auditService).record("TELEGRAM_LINK", "administrator");

        when(repository.findFirstByActiveTrue()).thenReturn(Optional.of(savedNew));
        assertEquals("44556677", service.resolveActiveChatId().orElseThrow());
        assertEquals("new-hash", service.activeAccountHash().orElseThrow());
    }

    @Test
    void unlinksActiveAccount() {
        TelegramLink active = new TelegramLink("active-hash", OffsetDateTime.now());
        active.setNotificationsEnabled(true);
        when(repository.findFirstByActiveTrue()).thenReturn(Optional.of(active));

        service.unlink();

        assertFalse(active.isActive());
        assertFalse(active.isNotificationsEnabled());
        assertNotNull(active.getUnlinkedAt());
        verify(auditService).record("TELEGRAM_UNLINK", "administrator");
    }

    @Test
    void updatesNotificationPreference() {
        TelegramLink active = new TelegramLink("active-hash", OffsetDateTime.now());
        active.setNotificationsEnabled(false);
        when(repository.findFirstByActiveTrue()).thenReturn(Optional.of(active));

        service.setNotifications(true);

        assertTrue(active.isNotificationsEnabled());
        verify(auditService).record("TELEGRAM_NOTIFICATIONS_UPDATE", "administrator");
    }
}
