package com.ucc.complaints.dto.report;

import java.util.List;

public record ReportSelection(List<String> complaintIds, String category) {
}
