package com.ucc.complaints.dto.status;

import com.ucc.complaints.model.StatusHistory;
import java.time.OffsetDateTime;

public record StatusHistoryResponse(
    String previousStatus, String newStatus, String comment,
    String changedBy, OffsetDateTime changedAt
) {
    public static StatusHistoryResponse from(StatusHistory history) {
        return new StatusHistoryResponse(history.getPreviousStatus(), history.getNewStatus(),
            history.getComment(), history.getChangedBy(), history.getChangedAt());
    }
}