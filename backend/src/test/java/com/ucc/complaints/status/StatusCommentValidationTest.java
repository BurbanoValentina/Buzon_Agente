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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class StatusCommentValidationTest {
    @Mock ComplaintRepository complaints;
    @Mock StatusHistoryRepository histories;
    @Mock MonthlyEventRepository monthlyEvents;
    @Mock AuditService auditService;
    @InjectMocks ComplaintStatusService service;

    @Test
    void requiresCommentForEveryManagementTransition() {
        Complaint complaint = new Complaint("C-1", true, "Aulas", "Asunto",
            "Descripción suficientemente larga", OffsetDateTime.now());
        org.springframework.test.util.ReflectionTestUtils.setField(complaint, "id", 1L);
        when(complaints.findByPublicId("C-1")).thenReturn(Optional.of(complaint));
        assertThrows(ComplaintValidationException.class, () -> service.transition("C-1",
            new StatusTransitionRequest(ComplaintStatus.COMPLETADA, " "), "admin"));
    }
}