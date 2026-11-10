package com.ucc.complaints.repository;

import com.ucc.complaints.model.MonthlyEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface MonthlyEventRepository extends JpaRepository<MonthlyEvent, Long> {
    List<MonthlyEvent> findByEventMonthBetween(LocalDate start, LocalDate end);
}
