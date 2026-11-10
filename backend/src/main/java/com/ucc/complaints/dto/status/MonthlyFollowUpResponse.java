package com.ucc.complaints.dto.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MonthlyFollowUpResponse(
    String complaintId, String eventType, LocalDate eventMonth, OffsetDateTime occurredAt
) { }