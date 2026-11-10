package com.ucc.complaints.status;

import com.ucc.complaints.dto.status.StatusTransitionRequest;
import com.ucc.complaints.model.*;
import com.ucc.complaints.repository.*;
import com.ucc.complaints.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ComplaintStatusServiceTest {
    @Mock ComplaintRepository complaints;
    @Mock StatusHistoryRepository histories;
    @Mock MonthlyEventRepository monthlyEvents;
    @Mock AuditService auditService;
    @InjectMocks ComplaintStatusService service;

    @Test
    void supportsCompletionReopeningAndRefinalization() {
        Complaint complaint = complaint();
        when(complaints.findByPublicId("C-1")).thenReturn(Optional.of(complaint));
        when(histories.findByComplaintIdOrderByChangedAtAsc(1L)).thenReturn(java.util.List.of());

        service.transition("C-1", new StatusTransitionRequest(
            ComplaintStatus.COMPLETADA, "Se atendió"), "admin");
        assertEquals(ComplaintStatus.COMPLETADA, complaint.getStatus());
        assertNotNull(complaint.getCompletedAt());

        StatusHistory reopened = new StatusHistory(complaint, ComplaintStatus.COMPLETADA,
            ComplaintStatus.PENDIENTE, "Revisar de nuevo", "admin", OffsetDateTime.now());
        when(histories.findByComplaintIdOrderByChangedAtAsc(1L)).thenReturn(java.util.List.of(reopened));
        service.transition("C-1", new StatusTransitionRequest(
            ComplaintStatus.PENDIENTE, "Revisar de nuevo"), "admin");
        service.transition("C-1", new StatusTransitionRequest(
            ComplaintStatus.COMPLETADA, "Conclusión final"), "admin");
        assertEquals(ComplaintStatus.COMPLETADA, complaint.getStatus());
        verify(monthlyEvents, atLeast(2)).save(any(MonthlyEvent.class));
    }

    @Test
    void rejectsInvalidTransition() {
        Complaint complaint = complaint();
        when(complaints.findByPublicId("C-1")).thenReturn(Optional.of(complaint));
        assertThrows(ComplaintValidationException.class, () -> service.transition("C-1",
            new StatusTransitionRequest(ComplaintStatus.PENDIENTE, "No aplica"), "admin"));
    }

    private Complaint complaint() {
        Complaint complaint = new Complaint("C-1", true, "Aulas", "Asunto",
            "Descripción suficientemente larga", OffsetDateTime.now());
        org.springframework.test.util.ReflectionTestUtils.setField(complaint, "id", 1L);
        return complaint;
    }
}