package com.ucc.complaints.service;

import com.ucc.complaints.dto.admin.*;
import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintQueryService {
    private final ComplaintRepository complaints;
    private final EvidenceRepository evidence;

    public ComplaintQueryService(ComplaintRepository complaints, EvidenceRepository evidence) {
        this.complaints = complaints;
        this.evidence = evidence;
    }

    public List<ComplaintSummary> list(ComplaintFilter filter) {
        return complaints.findAll().stream()
            .filter(ComplaintSpecifications.matches(filter))
            .map(ComplaintSummary::from)
            .collect(Collectors.toList());
    }

    public ComplaintDetail detail(String publicId) {
        Complaint complaint = complaints.findByPublicId(publicId)
            .orElseThrow(() -> new ComplaintValidationException("La queja no existe."));
        List<String> links = evidence.findByComplaintId(complaint.getId()).stream()
            .map(item -> item.getDriveFileId()).toList();
        return ComplaintDetail.from(complaint, links);
    }

    public List<Complaint> matching(ComplaintFilter filter) {
        return complaints.findAll().stream().filter(ComplaintSpecifications.matches(filter)).toList();
    }
}
