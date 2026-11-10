package com.ucc.complaints.report;

import com.ucc.complaints.dto.report.ReportSelection;
import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.Evidence;
import com.ucc.complaints.repository.ComplaintRepository;
import com.ucc.complaints.service.WordReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordReportServiceTest {
    @Mock ComplaintRepository complaints;

    @Test
    void selectsIndividualComplaintsAndByCategory() {
        Complaint selected = complaint("C-1", false, "Aulas");
        Complaint other = complaint("C-2", true, "Profesores");
        when(complaints.findAll()).thenReturn(List.of(selected, other));
        WordReportService service = new WordReportService(complaints, null, null);

        List<Complaint> individual = service.select(new ReportSelection(
            List.of("C-1"), null));
        List<Complaint> category = service.select(new ReportSelection(
            List.of(), "Profesores"));

        assertEquals(List.of(selected), individual);
        assertEquals(List.of(other), category);
    }

    @Test
    void aiInputExcludesIdentityEvidenceAndSensitiveIdentityFields() {
        Complaint identified = complaint("C-3", false, "Laboratorio");
        identified.setName("Nombre privado");
        identified.setSemester("8");
        Evidence evidence = evidence("imagen privada.jpg");
        WordReportService service = new WordReportService(complaints, null, null);

        String aiInput = service.buildAiInput(List.of(identified), List.of(evidence));

        assertTrue(aiInput.contains("Laboratorio"));
        assertTrue(aiInput.contains("C-3"));
        assertFalse(aiInput.contains("Nombre privado"));
        assertFalse(aiInput.contains("8"));
        assertFalse(aiInput.contains("imagen privada.jpg"));
        assertFalse(aiInput.toLowerCase().contains("evidence"));
    }

    private Complaint complaint(String publicId, boolean anonymous, String category) {
        Complaint complaint = new Complaint(publicId, anonymous, category,
            "Asunto válido", "Descripción suficientemente larga para el reporte",
            OffsetDateTime.parse("2026-11-05T10:00:00Z"));
        ReflectionTestUtils.setField(complaint, "id", Long.valueOf(publicId.substring(2)));
        return complaint;
    }

    private Evidence evidence(String fileName) {
        Evidence evidence = new Evidence();
        ReflectionTestUtils.setField(evidence, "fileName", fileName);
        ReflectionTestUtils.setField(evidence, "mimeType", "image/jpeg");
        return evidence;
    }
}
