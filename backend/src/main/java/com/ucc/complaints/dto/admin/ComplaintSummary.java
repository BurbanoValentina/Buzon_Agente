package com.ucc.complaints.dto.admin;

import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.ComplaintStatus;
import java.time.OffsetDateTime;

public record ComplaintSummary(
    String id,
    OffsetDateTime registeredAt,
    String category,
    String locationNumber,
    boolean anonymous,
    ComplaintStatus status
) {
    public static ComplaintSummary from(Complaint complaint) {
        return new ComplaintSummary(complaint.getPublicId(), complaint.getRegisteredAt(),
            complaint.getCategory(), complaint.getLocationNumber(),
            complaint.isAnonymous(), complaint.getStatus());
    }
}
