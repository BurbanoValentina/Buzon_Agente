package com.ucc.complaints.security;

import com.ucc.complaints.dto.security.*;
import com.ucc.complaints.model.AdministratorAccount;
import com.ucc.complaints.repository.AdministratorAccountRepository;
import com.ucc.complaints.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AccountLockoutTest {
    @Mock AdministratorAccountRepository accounts;
    @Mock PasswordEncoder encoder;
    @Mock AuditService audit;
    @InjectMocks AdminAuthenticationService service;

    @Test
    void locksForTwentyFourHoursAfterThreePasswordFailures() {
        AdministratorAccount account = new AdministratorAccount("administrator2026-1",
            "stored-hash", "answer-hash");
        when(accounts.findByUsername(account.getUsername())).thenReturn(Optional.of(account));
        when(encoder.matches(anyString(), eq("stored-hash"))).thenReturn(false);

        for (int attempt = 0; attempt < 3; attempt++) {
            assertThrows(ComplaintValidationException.class, () -> service.authenticate(
                new AdminLoginRequest(account.getUsername(), "bad")));
        }

        assertThrows(ComplaintValidationException.class, () -> service.authenticate(
            new AdminLoginRequest(account.getUsername(), "bad")));
        verify(audit).record("ACCOUNT_LOCKED", account.getUsername());
    }

    @Test
    void locksQuestionAttemptsUsingTheSameCounter() {
        AdministratorAccount account = new AdministratorAccount("administrator2026-1",
            "stored-hash", "answer-hash");
        when(accounts.findByUsername(account.getUsername())).thenReturn(Optional.of(account));
        when(encoder.matches(anyString(), eq("answer-hash"))).thenReturn(false);

        for (int attempt = 0; attempt < 3; attempt++) {
            assertThrows(ComplaintValidationException.class, () -> service.recover(
                new SecurityQuestionRequest(account.getUsername(), "wrong")));
        }
        assertThrows(ComplaintValidationException.class, () -> service.recover(
            new SecurityQuestionRequest(account.getUsername(), "wrong")));
        verify(audit).record("ACCOUNT_LOCKED", account.getUsername());
    }
}
