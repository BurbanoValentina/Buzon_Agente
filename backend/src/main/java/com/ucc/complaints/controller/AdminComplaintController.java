package com.ucc.complaints.controller;

import com.ucc.complaints.dto.admin.*;
import com.ucc.complaints.model.ComplaintStatus;
import com.ucc.complaints.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/complaints")
public class AdminComplaintController {
    private final ComplaintQueryService queryService;
    private final ExcelExportService excelExportService;

    public AdminComplaintController(ComplaintQueryService queryService, ExcelExportService excelExportService) {
        this.queryService = queryService;
        this.excelExportService = excelExportService;
    }

    @GetMapping
    public List<ComplaintSummary> list(
        @RequestParam(required = false) OffsetDateTime from,
        @RequestParam(required = false) OffsetDateTime to,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) ComplaintStatus status,
        @RequestParam(required = false) Boolean anonymous) {
        return queryService.list(new ComplaintFilter(from, to, category, status, anonymous));
    }

    @GetMapping("/{id}")
    public ComplaintDetail detail(@PathVariable String id) {
        return queryService.detail(id);
    }

    @GetMapping("/export.xlsx")
    public ResponseEntity<byte[]> export(
        @RequestParam(required = false) OffsetDateTime from,
        @RequestParam(required = false) OffsetDateTime to,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) ComplaintStatus status,
        @RequestParam(required = false) Boolean anonymous,
        @RequestHeader(value = "X-Admin-User", defaultValue = "administrator") String actor) throws IOException {
        byte[] content = excelExportService.export(new ComplaintFilter(from, to, category, status, anonymous), actor);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quejas.xlsx")
            .body(content);
    }
}
