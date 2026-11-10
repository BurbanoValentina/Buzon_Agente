package com.ucc.complaints.repository;

import com.ucc.complaints.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByComplaintIdOrderByChangedAtAsc(Long complaintId);
}
