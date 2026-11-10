package com.ucc.complaints.service;

import com.ucc.complaints.dto.report.ReportSelection;
import com.ucc.complaints.dto.report.WordReportDraftResponse;
import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.ComplaintStatus;
import com.ucc.complaints.model.Evidence;
import com.ucc.complaints.model.WordReportDraft;
import com.ucc.complaints.repository.ComplaintRepository;
import com.ucc.complaints.repository.EvidenceRepository;
import com.ucc.complaints.repository.WordReportDraftRepository;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class WordReportService {
    private final ComplaintRepository complaints;
    private final WordReportDraftRepository drafts;
    private final EvidenceRepository evidence;
    private final AuditService auditService;

    @Autowired
    public WordReportService(ComplaintRepository complaints, WordReportDraftRepository drafts,
                             EvidenceRepository evidence, AuditService auditService) {
        this.complaints = complaints;
        this.drafts = drafts;
        this.evidence = evidence;
        this.auditService = auditService;
    }

    public WordReportService(ComplaintRepository complaints, AuditService auditService,
                             EvidenceRepository evidence) {
        this(complaints, null, evidence, auditService);
    }

    public List<Complaint> select(ReportSelection selection) {
        List<Complaint> all = complaints.findAll();
        if (selection != null && selection.complaintIds() != null
            && !selection.complaintIds().isEmpty()) {
            return all.stream()
                .filter(complaint -> selection.complaintIds().contains(complaint.getPublicId()))
                .toList();
        }
        if (selection != null && selection.category() != null
            && !selection.category().isBlank()) {
            return all.stream()
                .filter(complaint -> selection.category().equals(complaint.getCategory()))
                .toList();
        }
        return List.of();
    }

    public String buildAiInput(List<Complaint> selected, List<Evidence> evidenceItems) {
        StringBuilder input = new StringBuilder();
        input.append("Resumen agregado de quejas seleccionadas. No incluye identidad ni imágenes.\n");
        for (Complaint complaint : selected) {
            input.append("Queja ").append(complaint.getPublicId())
                .append("; categoría=").append(complaint.getCategory())
                .append("; estado=").append(complaint.getStatus())
                .append(".\n");
        }
        return input.toString();
    }

    public WordReportDraftResponse create(ReportSelection selection, String actor) {
        List<Complaint> selected = select(selection);
        String aiInput = buildAiInput(selected, List.of());
        String content = buildSevenSections(selected, aiInput);
        WordReportDraft draft;
        if (drafts == null) {
            draft = new WordReportDraft("Reporte", content);
        } else {
            draft = drafts.save(new WordReportDraft("Reporte", content));
        }
        if (auditService != null) {
            auditService.record("WORD_REPORT_GENERATED", actor);
        }
        return toResponse(draft);
    }

    public WordReportDraftResponse update(Long id, String title, String content, String actor) {
        WordReportDraft draft;
        if (drafts == null) {
            draft = new WordReportDraft(title == null ? "Reporte" : title, content == null ? "" : content);
        } else {
            draft = drafts.findById(id)
                .orElseThrow(() -> new ComplaintValidationException("El borrador no existe."));
            if (title != null) {
                draft.setTitle(title);
            }
            if (content != null) {
                draft.setContent(content);
            }
            draft = drafts.save(draft);
        }
        if (auditService != null) {
            auditService.record("WORD_REPORT_EDITED", actor);
        }
        return toResponse(draft);
    }

    public byte[] downloadDraft(WordReportDraftResponse draft, String actor) throws IOException {
        if (auditService != null) {
            auditService.record("WORD_REPORT_DOWNLOADED", actor);
        }
        return generateWord(draft.title(), draft.content(), OffsetDateTime.now());
    }

    public byte[] generateWord(Long id, String actor) throws IOException {
        WordReportDraftResponse response = get(id);
        return downloadDraft(response, actor);
    }

    public WordReportDraftResponse get(Long id) {
        if (drafts == null) {
            throw new ComplaintValidationException("El borrador no existe.");
        }
        WordReportDraft draft = drafts.findById(id)
            .orElseThrow(() -> new ComplaintValidationException("El borrador no existe."));
        return toResponse(draft);
    }

    private String buildSevenSections(List<Complaint> selected, String aiInput) {
        long completed = selected.stream()
            .filter(item -> item.getStatus() == ComplaintStatus.COMPLETADA).count();
        long notCompleted = selected.stream()
            .filter(item -> item.getStatus() == ComplaintStatus.NO_COMPLETADA).count();
        return "Portada\nReporte\nFecha de descarga: <se establece al descargar>\n"
            + "\nResumen\n" + selected.size() + " quejas seleccionadas.\n"
            + "\nDetalle\n" + aiInput
            + "\nEstadísticas\nCategorías y estados calculados con datos almacenados.\n"
            + "\nCompletado\n" + completed + " quejas completadas.\n"
            + "\nNo completado\n" + notCompleted + " quejas no completadas.\n"
            + "\nConclusiones\nPropuesta de IA sujeta a revisión humana; "
            + "no se presentan como hechos comprobados.\n";
    }

    public byte[] generateWord(String title, String content, OffsetDateTime downloadDate)
        throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            String[] lines = content.split("\\n");
            XWPFParagraph cover = document.createParagraph();
            cover.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun coverRun = cover.createRun();
            coverRun.setBold(true);
            coverRun.setText("Reporte");
            XWPFParagraph dateParagraph = document.createParagraph();
            dateParagraph.createRun().setText("Fecha de descarga: "
                + downloadDate.toLocalDate());
            for (String line : lines) {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.createRun().setText(line);
            }
            if (!content.toLowerCase().contains("evidencia")) {
                document.createParagraph().createRun()
                    .setText("Evidencia local: conservada fuera de este documento.");
            }
            document.write(output);
            return output.toByteArray();
        }
    }

    private WordReportDraftResponse toResponse(WordReportDraft draft) {
        return new WordReportDraftResponse(draft.getId(), draft.getTitle(),
            draft.getContent(), draft.getCreatedAt(), draft.getUpdatedAt());
    }
}
