package com.ucc.complaints.service;

import com.ucc.complaints.dto.admin.ComplaintFilter;
import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.ComplaintStatus;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelExportServiceTest {
    @Mock
    private ComplaintQueryService queryService;

    @Mock
    private AuditService auditService;

    private ExcelExportService service;

    @BeforeEach
    void setUp() {
        service = new ExcelExportService(queryService, auditService);
    }

    @Test
    void exportsBothAnonymousAndIdentifiedComplaints() throws IOException {
        Complaint c1 = new Complaint("ID-1", true, "Infraestructura", "Asunto 1", "Desc 1", OffsetDateTime.now());
        Complaint c2 = new Complaint("ID-2", false, "Laboratorio", "Asunto 2", "Desc 2", OffsetDateTime.now());
        c2.setName("Estudiante Uno");
        c2.setSemester("4");
        c2.setLocationNumber("Lab 1");
        c2.setStatus(ComplaintStatus.COMPLETADA);
        c2.setCompletedAt(OffsetDateTime.now());

        when(queryService.matching(any())).thenReturn(List.of(c1, c2));

        byte[] result = service.export(new ComplaintFilter(null, null, null, null, null), "admin");

        assertNotNull(result);
        assertTrue(result.length > 0);
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertEquals("Quejas", wb.getSheetAt(0).getSheetName());
            assertEquals(3, wb.getSheetAt(0).getPhysicalNumberOfRows()); // Header + 2 data rows
        }
        verify(auditService).record("EXCEL_DOWNLOAD", "admin");
    }
}
