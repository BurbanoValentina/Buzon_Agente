package com.ucc.complaints.service;

import com.ucc.complaints.ai.AiAnalysisProvider;
import com.ucc.complaints.dto.ai.AiPredictionInput;
import com.ucc.complaints.dto.dashboard.DashboardResponse;
import com.ucc.complaints.dto.dashboard.DashboardResponse.DashboardPoint;
import com.ucc.complaints.dto.dashboard.DashboardResponse.PredictionResponse;
import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.ComplaintStatus;
import com.ucc.complaints.repository.ComplaintRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final ComplaintRepository complaints;
    private final PredictionPolicy predictionPolicy;
    private final AiAnalysisProvider aiAnalysisProvider;

    public DashboardService(ComplaintRepository complaints, PredictionPolicy predictionPolicy,
                            AiAnalysisProvider aiAnalysisProvider) {
        this.complaints = complaints;
        this.predictionPolicy = predictionPolicy;
        this.aiAnalysisProvider = aiAnalysisProvider;
    }

    public DashboardResponse getDashboard(String period, OffsetDateTime from, OffsetDateTime to) {
        List<Complaint> stored = complaints.findAll();
        List<Complaint> selected = stored.stream()
            .filter(complaint -> inRange(complaint.getRegisteredAt(), from, to))
            .toList();

        Map<String, Long> byCategory = selected.stream()
            .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));
        Map<ComplaintStatus, Long> byStatus = selected.stream()
            .collect(Collectors.groupingBy(Complaint::getStatus, Collectors.counting()));
        List<String> leadingCategories = byCategory.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .toList();
        long anonymous = selected.stream().filter(Complaint::isAnonymous).count();
        long identified = selected.size() - anonymous;

        List<DashboardPoint> weekly = weeklyPoints(selected, period);
        List<DashboardPoint> monthly = monthlyPoints(selected, from, to);
        List<PredictionResponse> predictions = buildPredictions(selected.size(), byCategory);

        return new DashboardResponse(selected.size(), byCategory, byStatus, leadingCategories,
            anonymous, identified, weekly, monthly, predictions);
    }

    private boolean inRange(OffsetDateTime value, OffsetDateTime from, OffsetDateTime to) {
        return (from == null || !value.isBefore(from)) && (to == null || !value.isAfter(to));
    }

    private List<DashboardPoint> weeklyPoints(List<Complaint> selected, String period) {
        Map<String, Long> counts = selected.stream().collect(Collectors.groupingBy(
            complaint -> complaint.getRegisteredAt().getYear() + "-W"
                + complaint.getRegisteredAt().get(WeekFields.ISO.weekOfWeekBasedYear()),
            Collectors.counting()));
        if (counts.isEmpty()) {
            return List.of();
        }
        return counts.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new DashboardPoint(entry.getKey(), null, entry.getValue()))
            .toList();
    }

    private List<DashboardPoint> monthlyPoints(List<Complaint> selected,
                                               OffsetDateTime from, OffsetDateTime to) {
        Map<YearMonth, Long> counts = selected.stream().collect(Collectors.groupingBy(
            complaint -> YearMonth.from(complaint.getRegisteredAt()),
            Collectors.counting()));
        YearMonth start = from == null && !counts.isEmpty()
            ? Collections.min(counts.keySet()) : (from == null ? YearMonth.now() : YearMonth.from(from));
        YearMonth end = to == null && !counts.isEmpty()
            ? Collections.max(counts.keySet()) : (to == null ? YearMonth.now() : YearMonth.from(to));
        if (start.isAfter(end)) {
            end = start;
        }
        List<DashboardPoint> points = new ArrayList<>();
        for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
            points.add(new DashboardPoint(null, month.toString(), counts.getOrDefault(month, 0L)));
        }
        return points;
    }

    private List<PredictionResponse> buildPredictions(long total, Map<String, Long> byCategory) {
        List<PredictionResponse> predictions = new ArrayList<>();
        if (predictionPolicy.eligibleForTotal(total)) {
            AiPredictionInput totalInput = new AiPredictionInput("TOTAL", total, total);
            predictions.add(new PredictionResponse("total", "Todas", "estimate",
                aiAnalysisProvider.estimateNextWeek(totalInput), "Datos insuficientes si el total es menor a cinco."));
        }
        byCategory.forEach((category, count) -> {
            if (predictionPolicy.eligibleForCategory(count)) {
                AiPredictionInput input = new AiPredictionInput(category, total, count);
                predictions.add(new PredictionResponse("category", category, "estimate",
                    aiAnalysisProvider.estimateNextWeek(input) + "; "
                        + aiAnalysisProvider.estimateNextMonth(input),
                    "Estimación de IA, no dato real."));
            }
        });
        return predictions;
    }
}
