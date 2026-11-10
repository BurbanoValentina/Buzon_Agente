package com.ucc.complaints.repository;

import com.ucc.complaints.model.SecurityChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import java.util.List;

public interface SecurityChallengeRepository extends JpaRepository<SecurityChallenge, Long> {
    List<SecurityChallenge> findByPurposeAndExpiresAtAfterAndConsumedAtIsNull(String purpose, OffsetDateTime now);
}
