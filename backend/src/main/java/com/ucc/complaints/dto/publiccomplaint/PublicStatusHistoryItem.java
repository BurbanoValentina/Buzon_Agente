package com.ucc.complaints.dto.publiccomplaint;

import java.time.OffsetDateTime;

public record PublicStatusHistoryItem(
    String previousStatus,
    String newStatus,
    String comment,
    OffsetDateTime changedAt
) {}
