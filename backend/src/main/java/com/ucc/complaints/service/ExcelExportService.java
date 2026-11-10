package com.ucc.complaints.service;

import com.ucc.complaints.dto.admin.ComplaintFilter;
import com.ucc.complaints.model.Complaint;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ExcelExportService {
    static final List<String> HEADERS = List.of("Identificador", "Fecha", "Categoría", "Número",
        "Tipo de usuario", "Nombre", "Semestre", "Asunto", "Descripción", "Estado",
        "Fecha de cumplimiento", "Enlace a evidencia");

    private final ComplaintQueryService queryService;
    private final AuditService auditService;

    public ExcelExportService(ComplaintQueryService queryService, AuditService auditService) {
        this.queryService = queryService;
        this.auditService = auditService;
    }

    public byte[] export(ComplaintFilter filter, String actor) throws IOException {
        List<Complaint> complaints = queryService.matching(filter);
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Quejas");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.size(); i++) {
                header.createCell(i).setCellValue(HEADERS.get(i));
            }
            for (int rowIndex = 0; rowIndex < complaints.size(); rowIndex++) {
                Complaint complaint = complaints.get(rowIndex);
                Row row = sheet.createRow(rowIndex + 1);
                boolean identified = !complaint.isAnonymous();
                set(row, 0, complaint.getPublicId());
                set(row, 1, format(complaint.getRegisteredAt()));
                set(row, 2, complaint.getCategory());
                set(row, 3, complaint.getLocationNumber());
                set(row, 4, identified ? "Identificado" : "Anónimo");
                set(row, 5, identified ? complaint.getName() : null);
                set(row, 6, identified ? complaint.getSemester() : null);
                set(row, 7, complaint.getSubject());
                set(row, 8, complaint.getDescription());
                set(row, 9, complaint.getStatus().name());
                set(row, 10, format(complaint.getCompletedAt()));
                set(row, 11, "");
            }
            workbook.write(output);
            auditService.record("EXCEL_DOWNLOAD", actor);
            return output.toByteArray();
        }
    }

    private void set(Row row, int index, String value) {
        row.createCell(index).setCellValue(value == null ? "" : value);
    }

    private String format(OffsetDateTime date) {
        return date == null ? "" : date.toString();
    }
}
