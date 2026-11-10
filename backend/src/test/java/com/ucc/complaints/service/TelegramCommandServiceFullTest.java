package com.ucc.complaints.service;

import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.ComplaintStatus;
import com.ucc.complaints.model.TelegramLink;
import com.ucc.complaints.repository.ComplaintRepository;
import com.ucc.complaints.repository.TelegramLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramCommandServiceFullTest {
    @Mock
    private TelegramLinkRepository links;

    @Mock
    private ComplaintRepository complaints;

    private TelegramCommandService service;

    @BeforeEach
    void setUp() {
        service = new TelegramCommandService(links, complaints);
    }

    @Test
    void executeHelp() {
        assertEquals("Comandos disponibles: Ayuda, Resumen, Pendientes, Mes. Solo lectura.",
            service.execute("acc", "/ayuda"));
    }

    @Test
    void executeUnlinkedAccount() {
        when(links.findFirstByActiveTrue()).thenReturn(Optional.empty());
        assertEquals("Cuenta no vinculada.", service.execute("acc", "/resumen"));
    }

    @Test
    void executeCommandsForLinkedAccount() {
        TelegramLink link = new TelegramLink("hash-123", OffsetDateTime.now());
        when(links.findFirstByActiveTrue()).thenReturn(Optional.of(link));
        when(complaints.count()).thenReturn(5L);

        Complaint c = new Complaint("ID-1", true, "Infraestructura", "Asunto", "Desc", OffsetDateTime.now());
        when(complaints.findByStatus(ComplaintStatus.PENDIENTE)).thenReturn(List.of(c));
        when(complaints.findAll()).thenReturn(List.of(c));

        assertTrue(service.execute("hash-123", "/resumen").contains("5 quejas"));
        assertTrue(service.execute("hash-123", "/pendientes").contains("1"));
        assertTrue(service.execute("hash-123", "/mes").contains("1"));
        assertEquals("Comando no reconocido. Usa Ayuda.", service.execute("hash-123", "/desconocido"));
    }
}
