package com.ucc.complaints.controller;

import com.ucc.complaints.dto.report.ReportSelection;
import com.ucc.complaints.dto.report.WordReportDraftResponse;
import com.ucc.complaints.service.WordReportService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reports/word")
public class WordReportController {
    private final WordReportService service;

    public WordReportController(WordReportService service) {
        this.service = service;
    }

    @PostMapping("/drafts")
    public WordReportDraftResponse create(@RequestBody ReportSelection selection,
                                          @RequestHeader(value = "X-Admin-User", defaultValue = "administrator") String actor) {
        return service.create(selection, actor);
    }

    @PutMapping("/drafts/{id}")
    public WordReportDraftResponse update(@PathVariable Long id,
                                          @RequestBody WordReportDraftResponse draft,
                                          @RequestHeader(value = "X-Admin-User", defaultValue = "administrator") String actor) {
        return service.update(id, draft.title(), draft.content(), actor);
    }

    @GetMapping("/drafts/{id}")
    public WordReportDraftResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/drafts/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id,
                                           @RequestHeader(value = "X-Admin-User", defaultValue = "administrator") String actor) throws Exception {
        byte[] content = service.generateWord(id, actor);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte.docx")
            .contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            .body(content);
    }
}
