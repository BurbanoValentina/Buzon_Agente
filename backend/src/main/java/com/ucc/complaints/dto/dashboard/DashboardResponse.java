package com.ucc.complaints.dto.dashboard;

import com.ucc.complaints.model.ComplaintStatus;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
    long total,
    Map<String, Long> byCategory,
    Map<ComplaintStatus, Long> byStatus,
    List<String> leadingCategories,
    long anonymous,
    long identified,
    List<DashboardPoint> weekly,
    List<DashboardPoint> monthly,
    List<PredictionResponse> predictions
) {
    public record DashboardPoint(String week, String month, long count) {
    }

    public record PredictionResponse(String scope, String category, String estimateType,
                                     String estimate, String limitation) {
    }
}
