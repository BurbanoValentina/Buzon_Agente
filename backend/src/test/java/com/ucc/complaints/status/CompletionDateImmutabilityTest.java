package com.ucc.complaints.status;

import com.ucc.complaints.dto.status.StatusTransitionRequest;
import com.ucc.complaints.model.*;
import com.ucc.complaints.repository.*;
import com.ucc.complaints.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CompletionDateImmutabilityTest {
    @Mock ComplaintRepository complaints;
    @Mock StatusHistoryRepository histories;
    @Mock MonthlyEventRepository monthlyEvents;
    @Mock AuditService auditService;
    @InjectMocks ComplaintStatusService service;

    @Test
    void preservesFirstCompletionDateWhileKeepingMonthlyEvents() {
        Complaint complaint = new Complaint("C-1", true, "Aulas", "Asunto",
            "Descripción suficientemente larga", OffsetDateTime.now());
        org.springframework.test.util.ReflectionTestUtils.setField(complaint, "id", 1L);
        when(complaints.findByPublicId("C-1")).thenReturn(Optional.of(complaint));
        when(histories.findByComplaintIdOrderByChangedAtAsc(1L)).thenReturn(List.of());
        service.transition("C-1", new StatusTransitionRequest(
            ComplaintStatus.COMPLETADA, "Primera conclusión"), "admin");
        OffsetDateTime firstDate = complaint.getCompletedAt();
        service.transition("C-1", new StatusTransitionRequest(
            ComplaintStatus.PENDIENTE, "Reapertura"), "admin");
        service.transition("C-1", new StatusTransitionRequest(
            ComplaintStatus.COMPLETADA, "Nueva conclusión"), "admin");
        assertEquals(firstDate, complaint.getCompletedAt());
        verify(monthlyEvents, atLeast(2)).save(any(MonthlyEvent.class));
    }
}