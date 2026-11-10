package com.ucc.complaints.security;

import com.ucc.complaints.dto.security.AdminLoginRequest;
import com.ucc.complaints.model.AdministratorAccount;
import com.ucc.complaints.repository.AdministratorAccountRepository;
import com.ucc.complaints.service.AuditService;
import com.ucc.complaints.service.ComplaintValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AdminAuthenticationTest {
    @Mock AdministratorAccountRepository accounts;
    @Mock PasswordEncoder encoder;
    @Mock AuditService audit;
    @InjectMocks AdminAuthenticationService service;

    @Test
    void authenticatesOnlyTheStoredAdministrator() {
        AdministratorAccount account = new AdministratorAccount("administrator2026-1",
            "stored-hash", "answer-hash");
        when(accounts.findByUsername("administrator2026-1")).thenReturn(Optional.of(account));
        when(encoder.matches("RepresentanteIng2026", "stored-hash")).thenReturn(true);

        assertSame(account, service.authenticate(new AdminLoginRequest(
            "administrator2026-1", "RepresentanteIng2026")));
        verify(audit).record("LOGIN_SUCCESS", "administrator2026-1");
    }

    @Test
    void rejectsUnknownAccountWithoutProvidingRegistration() {
        when(accounts.findByUsername("other")).thenReturn(Optional.empty());
        assertThrows(ComplaintValidationException.class, () -> service.authenticate(
            new AdminLoginRequest("other", "RepresentanteIng2026")));
    }
}
