package com.ucc.complaints.repository;

import com.ucc.complaints.model.AdministratorAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdministratorAccountRepository extends JpaRepository<AdministratorAccount, Long> {
    Optional<AdministratorAccount> findByUsername(String username);
}
