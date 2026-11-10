package com.ucc.complaints.security;

import com.ucc.complaints.controller.AdminAccountController;
import com.ucc.complaints.controller.AdminSessionController;
import com.ucc.complaints.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AdminSessionController.class, AdminAccountController.class})
@Import(SecurityConfig.class)
class AdminAccountControllerTest {
    @Autowired MockMvc mvc;
    @MockBean AdminAuthenticationService authenticationService;
    @MockBean AuditService auditService;

    @Test
    void exposesOnlyTheFixedSecurityQuestionAndProtectsConfiguration() throws Exception {
        mvc.perform(get("/api/v1/admin/account/security-question"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/auth/login")
            .contentType("application/json")
            .content("{\"username\":\"\",\"password\":\"\"}"))
            .andExpect(status().isBadRequest());
    }
}
