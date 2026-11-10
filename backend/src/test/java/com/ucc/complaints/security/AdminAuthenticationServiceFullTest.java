package com.ucc.complaints.security;

import com.ucc.complaints.dto.security.ChangeCredentialsRequest;
import com.ucc.complaints.dto.security.SecurityQuestionRequest;
import com.ucc.complaints.model.AdministratorAccount;
import com.ucc.complaints.repository.AdministratorAccountRepository;
import com.ucc.complaints.service.AuditService;
import com.ucc.complaints.service.ComplaintValidationException;
import com.ucc.complaints.telegram.TelegramChallengeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuthenticationServiceFullTest {
    @Mock
    private AdministratorAccountRepository accounts;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @Mock
    private TelegramChallengeService challengeService;

    private AdminAuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new AdminAuthenticationService(accounts, passwordEncoder, auditService, challengeService);
    }

    @Test
    void telegramOtpIssueAndValidate() {
        when(challengeService.issueLoginOtp("LOGIN", "acc-1")).thenReturn("123456");
        when(challengeService.validate("LOGIN", "acc-1", "123456")).thenReturn(true);
        when(challengeService.validate("LOGIN", "acc-1", "000000")).thenReturn(false);

        String code = service.issueTelegramOtp("acc-1");
        assertEquals("123456", code);
        verify(auditService).record("TELEGRAM_LOGIN_OTP_ISSUED", "administrator");

        assertTrue(service.validateTelegramOtp("acc-1", "123456"));
        verify(auditService).record("TELEGRAM_LOGIN_OTP_SUCCESS", "administrator");

        assertFalse(service.validateTelegramOtp("acc-1", "000000"));
        verify(auditService).record("TELEGRAM_LOGIN_OTP_FAILED", "administrator");
    }

    @Test
    void recoverAccountWithSecurityQuestion() {
        AdministratorAccount account = new AdministratorAccount("administrator2026-1", "hash", "ans-hash");
        when(accounts.findByUsername("administrator2026-1")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("Ingeniería", "ans-hash")).thenReturn(true);

        AdministratorAccount recovered = service.recover(new SecurityQuestionRequest("administrator2026-1", "Ingeniería"));

        assertNotNull(recovered);
        verify(auditService).record("SECURITY_QUESTION_SUCCESS", "administrator2026-1");
    }

    @Test
    void recoverFailsWithWrongAnswerAndLocksAfterThreeAttempts() {
        AdministratorAccount account = new AdministratorAccount("administrator2026-1", "hash", "ans-hash");
        account.setFailedAttempts(2);
        when(accounts.findByUsername("administrator2026-1")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("Incorrecta", "ans-hash")).thenReturn(false);

        assertThrows(ComplaintValidationException.class,
            () -> service.recover(new SecurityQuestionRequest("administrator2026-1", "Incorrecta")));

        assertEquals(3, account.getFailedAttempts());
        assertNotNull(account.getLockedUntil());
        verify(auditService).record("ACCOUNT_LOCKED", "administrator2026-1");
    }

    @Test
    void changeCredentialsSuccess() {
        AdministratorAccount account = new AdministratorAccount("administrator2026-1", "hash", "ans-hash");
        when(accounts.findByUsername("administrator2026-1")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("RepresentanteIng2026", "hash")).thenReturn(true);
        when(passwordEncoder.encode("RepresentanteIng2027")).thenReturn("new-pass-hash");
        when(passwordEncoder.encode("NuevaRespuesta")).thenReturn("new-ans-hash");

        ChangeCredentialsRequest request = new ChangeCredentialsRequest(
            "administrator2026-2",
            "RepresentanteIng2026",
            "RepresentanteIng2027",
            "NuevaRespuesta"
        );

        AdministratorAccount updated = service.changeCredentials("administrator2026-1", request);

        assertEquals("administrator2026-2", updated.getUsername());
        assertEquals("new-pass-hash", updated.getPasswordHash());
        assertEquals("new-ans-hash", updated.getSecurityAnswerHash());
        verify(auditService).record("CREDENTIALS_CHANGED", "administrator2026-2");
    }

    @Test
    void blocksLockedAccount() {
        AdministratorAccount account = new AdministratorAccount("administrator2026-1", "hash", "ans-hash");
        account.setLockedUntil(OffsetDateTime.now().plusHours(12));
        when(accounts.findByUsername("administrator2026-1")).thenReturn(Optional.of(account));

        ChangeCredentialsRequest request = new ChangeCredentialsRequest(
            "administrator2026-2", "RepresentanteIng2026", "RepresentanteIng2027", "Ans"
        );

        assertThrows(ComplaintValidationException.class,
            () -> service.changeCredentials("administrator2026-1", request));
        verify(auditService).record("ACCOUNT_LOCKED_ACCESS", "administrator2026-1");
    }
}
