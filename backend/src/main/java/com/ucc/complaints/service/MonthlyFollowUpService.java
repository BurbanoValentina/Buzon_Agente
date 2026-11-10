package com.ucc.complaints.service;

import com.ucc.complaints.dto.status.MonthlyFollowUpResponse;
import com.ucc.complaints.model.MonthlyEvent;
import com.ucc.complaints.repository.MonthlyEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MonthlyFollowUpService {
    private final MonthlyEventRepository monthlyEvents;

    public MonthlyFollowUpService(MonthlyEventRepository monthlyEvents) {
        this.monthlyEvents = monthlyEvents;
    }

    public List<MonthlyFollowUpResponse> find(LocalDate from, LocalDate to) {
        LocalDate start = from == null ? LocalDate.of(1970, 1, 1) : from.withDayOfMonth(1);
        LocalDate end = to == null ? LocalDate.of(9999, 12, 1) : to.withDayOfMonth(1);
        return monthlyEvents.findByEventMonthBetween(start, end).stream()
            .map(this::toResponse).toList();
    }

    private MonthlyFollowUpResponse toResponse(MonthlyEvent event) {
        return new MonthlyFollowUpResponse(event.getComplaint().getPublicId(),
            event.getEventType(), event.getEventMonth(), event.getOccurredAt());
    }
}