package com.ucc.complaints.service;

import com.ucc.complaints.dto.admin.ComplaintDetail;
import com.ucc.complaints.dto.admin.ComplaintFilter;
import com.ucc.complaints.dto.admin.ComplaintSummary;
import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.Evidence;
import com.ucc.complaints.repository.ComplaintRepository;
import com.ucc.complaints.repository.EvidenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintQueryServiceTest {
    @Mock
    private ComplaintRepository complaints;

    @Mock
    private EvidenceRepository evidence;

    private ComplaintQueryService service;

    @BeforeEach
    void setUp() {
        service = new ComplaintQueryService(complaints, evidence);
    }

    @Test
    void listsAndFiltersComplaints() {
        Complaint c = new Complaint("ID-1", true, "Infraestructura", "Asunto", "Descripcion", OffsetDateTime.now());
        when(complaints.findAll()).thenReturn(List.of(c));

        List<ComplaintSummary> summaries = service.list(new ComplaintFilter(null, null, "Infraestructura", null, null));
        assertEquals(1, summaries.size());
        assertEquals("ID-1", summaries.get(0).id());

        List<Complaint> matching = service.matching(new ComplaintFilter(null, null, "Bienestar", null, null));
        assertTrue(matching.isEmpty());
    }

    @Test
    void getsDetailWithEvidenceLinks() throws Exception {
        Complaint c = new Complaint("ID-1", false, "Infraestructura", "Asunto", "Descripcion", OffsetDateTime.now());
        Field idField = Complaint.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(c, 10L);

        Evidence e = new Evidence();
        Field driveIdField = Evidence.class.getDeclaredField("driveFileId");
        driveIdField.setAccessible(true);
        driveIdField.set(e, "drive-file-123");

        when(complaints.findByPublicId("ID-1")).thenReturn(Optional.of(c));
        when(evidence.findByComplaintId(10L)).thenReturn(List.of(e));

        ComplaintDetail detail = service.detail("ID-1");
        assertNotNull(detail);
        assertEquals("ID-1", detail.id());
        assertEquals(List.of("drive-file-123"), detail.evidenceLinks());
    }

    @Test
    void throwsWhenComplaintNotFound() {
        when(complaints.findByPublicId("NON-EXISTENT")).thenReturn(Optional.empty());

        assertThrows(ComplaintValidationException.class, () -> service.detail("NON-EXISTENT"));
    }
}
