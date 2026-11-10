package com.ucc.complaints.repository;

import com.ucc.complaints.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Optional<Complaint> findByPublicId(String publicId);
    List<Complaint> findByStatus(ComplaintStatus status);
    List<Complaint> findByCategory(String category);
}
