package com.ucc.complaints.dto.publiccomplaint;

import java.time.OffsetDateTime;
import java.util.List;

public record PublicComplaintStatusResponse(
    String publicId,
    String category,
    String status,
    String subject,
    OffsetDateTime registeredAt,
    OffsetDateTime completedAt,
    List<PublicStatusHistoryItem> history
) {}
