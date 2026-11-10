package com.ucc.complaints.security;

import com.ucc.complaints.controller.AdminSessionController;
import com.ucc.complaints.dto.security.AdminLoginRequest;
import com.ucc.complaints.model.AdministratorAccount;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class InactivityTimeoutTest {
    @Test
    void administrativeSessionUsesTenHourInactivityTimeout() {
        AdminAuthenticationService service = mock(AdminAuthenticationService.class);
        AdministratorAccount account = new AdministratorAccount(
            "administrator-period", "hash", "answer");
        when(service.authenticate(any(AdminLoginRequest.class))).thenReturn(account);
        MockHttpSession session = new MockHttpSession();

        new AdminSessionController(service).login(
            new AdminLoginRequest("administrator-period", "RepresentanteIng0000"), session);

        assertEquals(10 * 60 * 60, session.getMaxInactiveInterval());
    }
}
