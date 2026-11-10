package com.ucc.complaints.service;

import com.ucc.complaints.dto.status.MonthlyFollowUpResponse;
import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.MonthlyEvent;
import com.ucc.complaints.repository.MonthlyEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyFollowUpServiceTest {
    @Mock
    private MonthlyEventRepository repository;

    private MonthlyFollowUpService service;

    @BeforeEach
    void setUp() {
        service = new MonthlyFollowUpService(repository);
    }

    @Test
    void findsAndMapsMonthlyEvents() {
        Complaint complaint = new Complaint("C-1", true, "Infraestructura", "Asunto", "Desc", OffsetDateTime.now());
        MonthlyEvent event = new MonthlyEvent(complaint, "REGISTRO", LocalDate.of(2026, 3, 1), OffsetDateTime.now());

        when(repository.findByEventMonthBetween(any(), any())).thenReturn(List.of(event));

        List<MonthlyFollowUpResponse> result = service.find(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1));
        assertEquals(1, result.size());
        assertEquals("C-1", result.get(0).complaintId());
        assertEquals("REGISTRO", result.get(0).eventType());
    }
}
