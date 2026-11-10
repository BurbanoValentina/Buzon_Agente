package com.ucc.complaints.security;

import com.ucc.complaints.dto.security.*;
import com.ucc.complaints.model.AdministratorAccount;
import com.ucc.complaints.repository.AdministratorAccountRepository;
import com.ucc.complaints.service.AuditService;
import com.ucc.complaints.service.ComplaintValidationException;
import com.ucc.complaints.telegram.TelegramChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.regex.Pattern;

@Service
public class AdminAuthenticationService {
    public static final String SESSION_USERNAME = "ADMIN_USERNAME";
    public static final String SECURITY_QUESTION = "¿Qué representante eres?";
    private static final Pattern USERNAME = Pattern.compile("^administrator\\S+$");
    private static final Pattern PASSWORD = Pattern.compile("^RepresentanteIng\\d{4}$");
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCKOUT_HOURS = 24;

    private final AdministratorAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final TelegramChallengeService challengeService;

    public AdminAuthenticationService(AdministratorAccountRepository accounts,
                                       PasswordEncoder passwordEncoder,
                                       AuditService auditService) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.challengeService = null;
    }

    @Autowired
    public AdminAuthenticationService(AdministratorAccountRepository accounts,
                                       PasswordEncoder passwordEncoder,
                                       AuditService auditService,
                                       TelegramChallengeService challengeService) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.challengeService = challengeService;
    }

    public String issueTelegramOtp(String accountId) {
        return issueTelegramOtp("LOGIN", accountId);
    }

    public String issueTelegramOtp(String purpose, String accountId) {
        if (challengeService == null) {
            throw new ComplaintValidationException("Telegram no está configurado.");
        }
        String code = challengeService.issueLoginOtp(purpose, accountId);
        auditService.record("TELEGRAM_LOGIN_OTP_ISSUED", "administrator");
        return code;
    }

    public boolean validateTelegramOtp(String accountId, String code) {
        return validateTelegramOtp("LOGIN", accountId, code);
    }

    public boolean validateTelegramOtp(String purpose, String accountId, String code) {
        if (challengeService == null) {
            return false;
        }
        boolean valid = challengeService.validate(purpose, accountId, code);
        auditService.record(valid ? "TELEGRAM_LOGIN_OTP_SUCCESS" : "TELEGRAM_LOGIN_OTP_FAILED",
            "administrator");
        return valid;
    }

    public void recordFallbackActivated(String username) {
        auditService.record("TELEGRAM_OTP_FALLBACK_ACTIVATED", username);
    }

    @Transactional
    public AdministratorAccount authenticate(AdminLoginRequest request) {
        String normalizedUsername = request.username() == null ? "" : request.username().trim();
        AdministratorAccount account = accounts.findByUsername(normalizedUsername)
            .orElseThrow(() -> failedLogin());
        ensureNotLocked(account);
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            registerFailure(account, "LOGIN_FAILED");
            throw new ComplaintValidationException("Las credenciales no son válidas.");
        }
        account.setFailedAttempts(0);
        account.setLockedUntil(null);
        accounts.save(account);
        auditService.record("LOGIN_SUCCESS", account.getUsername());
        return account;
    }

    @Transactional
    public AdministratorAccount recover(SecurityQuestionRequest request) {
        String normalizedUsername = request.username() == null ? "" : request.username().trim();
        String normalizedAnswer = request.answer() == null ? "" : request.answer().trim();
        AdministratorAccount account = accounts.findByUsername(normalizedUsername)
            .orElseThrow(() -> failedLogin());
        ensureNotLocked(account);
        if (!passwordEncoder.matches(normalizedAnswer, account.getSecurityAnswerHash())) {
            registerFailure(account, "SECURITY_QUESTION_FAILED");
            throw new ComplaintValidationException("La respuesta no es válida.");
        }
        account.setFailedAttempts(0);
        account.setLockedUntil(null);
        accounts.save(account);
        auditService.record("SECURITY_QUESTION_SUCCESS", account.getUsername());
        return account;
    }

    @Transactional
    public AdministratorAccount changeCredentials(String currentUsername,
                                                  ChangeCredentialsRequest request) {
        AdministratorAccount account = accounts.findByUsername(currentUsername)
            .orElseThrow(() -> new ComplaintValidationException("La cuenta no existe."));
        ensureNotLocked(account);
        validateCredentials(request.username(), request.newPassword());
        if (!passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
            registerFailure(account, "CREDENTIAL_CHANGE_FAILED");
            throw new ComplaintValidationException("La contraseña actual no es válida.");
        }
        account.setUsername(request.username());
        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        account.setSecurityAnswerHash(passwordEncoder.encode(request.securityAnswer()));
        account.setFailedAttempts(0);
        account.setLockedUntil(null);
        accounts.save(account);
        auditService.record("CREDENTIALS_CHANGED", account.getUsername());
        return account;
    }

    public void validateCredentials(String username, String password) {
        if (!USERNAME.matcher(username).matches()) {
            throw new ComplaintValidationException(
                "El usuario debe comenzar por administrator y contener el período.");
        }
        if (!PASSWORD.matcher(password).matches()) {
            throw new ComplaintValidationException(
                "La contraseña debe comenzar por RepresentanteIng y terminar en cuatro dígitos.");
        }
    }

    private void ensureNotLocked(AdministratorAccount account) {
        if (account.getLockedUntil() != null
            && account.getLockedUntil().isAfter(OffsetDateTime.now())) {
            auditService.record("ACCOUNT_LOCKED_ACCESS", account.getUsername());
            throw new ComplaintValidationException("La cuenta está bloqueada durante 24 horas.");
        }
    }

    private void registerFailure(AdministratorAccount account, String event) {
        int attempts = account.getFailedAttempts() + 1;
        account.setFailedAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            account.setLockedUntil(OffsetDateTime.now().plusHours(LOCKOUT_HOURS));
            auditService.record("ACCOUNT_LOCKED", account.getUsername());
        } else {
            auditService.record(event, account.getUsername());
        }
        accounts.save(account);
    }

    private ComplaintValidationException failedLogin() {
        auditService.record("LOGIN_FAILED", null);
        return new ComplaintValidationException("Las credenciales no son válidas.");
    }
}
