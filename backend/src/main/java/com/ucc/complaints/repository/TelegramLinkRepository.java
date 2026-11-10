package com.ucc.complaints.repository;

import com.ucc.complaints.model.TelegramLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TelegramLinkRepository extends JpaRepository<TelegramLink, Long> {
    Optional<TelegramLink> findByTelegramAccountHash(String hash);
    Optional<TelegramLink> findFirstByActiveTrue();
}
