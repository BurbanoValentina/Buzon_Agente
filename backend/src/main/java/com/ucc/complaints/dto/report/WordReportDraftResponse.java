package com.ucc.complaints.dto.report;

import java.time.OffsetDateTime;

public record WordReportDraftResponse(Long id, String title, String content,
                                      OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
