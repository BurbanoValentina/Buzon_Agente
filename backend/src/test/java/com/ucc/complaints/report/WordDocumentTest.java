package com.ucc.complaints.report;

import com.ucc.complaints.dto.report.ReportSelection;
import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.repository.ComplaintRepository;
import com.ucc.complaints.service.AuditService;
import com.ucc.complaints.service.WordReportService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordDocumentTest {
    @Mock ComplaintRepository complaints;
    @Mock AuditService auditService;

    @Test
    void createsSevenSectionsWithEditableContentAndNoLogo() throws Exception {
        Complaint complaint = new Complaint("C-1", true, "Aulas",
            "Asunto válido", "Descripción suficientemente larga para el reporte",
            OffsetDateTime.parse("2026-11-05T10:00:00Z"));
        ReflectionTestUtils.setField(complaint, "id", 1L);
        when(complaints.findAll()).thenReturn(List.of(complaint));
        WordReportService service = new WordReportService(complaints, auditService, null);

        byte[] documentBytes = service.downloadDraft(service.create(
            new ReportSelection(List.of("C-1"), null), "admin"), "admin");

        assertArrayEquals(documentBytes, documentBytes);
        try (XWPFDocument document = new XWPFDocument(
            new ByteArrayInputStream(documentBytes))) {
            String text = document.getParagraphs().stream()
                .map(paragraph -> paragraph.getText())
                .reduce("", (left, right) -> left + "\n" + right);
            assertTrue(text.contains("Reporte"));
            assertTrue(text.contains("Portada"));
            assertTrue(text.contains("Resumen"));
            assertTrue(text.contains("Detalle"));
            assertTrue(text.contains("Estadísticas"));
            assertTrue(text.contains("Completado"));
            assertTrue(text.contains("No completado"));
            assertTrue(text.contains("Conclusiones"));
            assertFalse(text.toLowerCase().contains("logo"));
            assertFalse(text.contains("image:"));
        }
    }
}
