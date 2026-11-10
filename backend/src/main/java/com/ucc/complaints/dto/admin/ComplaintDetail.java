package com.ucc.complaints.dto.admin;

import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.ComplaintStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record ComplaintDetail(
    String id,
    OffsetDateTime registeredAt,
    String category,
    String locationNumber,
    boolean anonymous,
    String name,
    String semester,
    String subject,
    String description,
    ComplaintStatus status,
    OffsetDateTime completedAt,
    List<String> evidenceLinks
) {
    public static ComplaintDetail from(Complaint complaint, List<String> evidenceLinks) {
        return new ComplaintDetail(complaint.getPublicId(), complaint.getRegisteredAt(),
            complaint.getCategory(), complaint.getLocationNumber(), complaint.isAnonymous(),
            complaint.isAnonymous() ? null : complaint.getName(),
            complaint.isAnonymous() ? null : complaint.getSemester(),
            complaint.getSubject(), complaint.getDescription(), complaint.getStatus(),
            complaint.getCompletedAt(), evidenceLinks);
    }
}
