package com.ucc.complaints.security;

import com.ucc.complaints.controller.AdminAccountController;
import com.ucc.complaints.controller.AdminSessionController;
import com.ucc.complaints.model.AdministratorAccount;
import com.ucc.complaints.service.AuditService;
import com.ucc.complaints.service.TelegramLinkService;
import com.ucc.complaints.telegram.TelegramChallengeService;
import com.ucc.complaints.telegram.TelegramClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AdminSessionController.class, AdminAccountController.class})
@AutoConfigureMockMvc(addFilters = false)
class AdminSessionOtpFlowTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private AdminAuthenticationService authenticationService;

    @MockBean
    private AuditService auditService;

    @MockBean
    private TelegramLinkService telegramLinkService;

    @MockBean
    private TelegramClient telegramClient;

    @MockBean
    private TelegramChallengeService challengeService;

    @Test
    void requiresTelegramOtpOnLoginResendAndFallbackAfterThreeErrors() throws Exception {
        AdministratorAccount account = new AdministratorAccount("administrator2026-1", "hash", "ans");
        when(authenticationService.authenticate(any())).thenReturn(account);
        when(telegramLinkService.status()).thenReturn(new TelegramLinkService.TelegramLinkStatus(true, true));
        when(telegramLinkService.resolveActiveChatId()).thenReturn(Optional.of("999000"));
        when(telegramLinkService.activeAccountHash()).thenReturn(Optional.of("hash-acc"));
        when(authenticationService.issueTelegramOtp("LOGIN", "hash-acc")).thenReturn("654321");

        MvcResult loginResult = mvc.perform(post("/api/v1/admin/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"administrator2026-1\",\"password\":\"RepresentanteIng2026\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.otpRequired").value(true))
            .andExpect(jsonPath("$.expiresInSeconds").value(15))
            .andReturn();

        verify(telegramClient).sendMessage(eq("999000"), contains("654321"));
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mvc.perform(post("/api/v1/admin/auth/otp/resend").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.otpRequired").value(true));

        when(authenticationService.validateTelegramOtp("LOGIN", "hash-acc", "000000")).thenReturn(false);
        when(challengeService.requiresSecurityFallback("hash-acc")).thenReturn(false).thenReturn(false).thenReturn(true);

        mvc.perform(post("/api/v1/admin/auth/otp/verify")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"code\":\"000000\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fallbackRequired").value(false));

        mvc.perform(post("/api/v1/admin/auth/otp/verify")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"code\":\"000000\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fallbackRequired").value(false));

        mvc.perform(post("/api/v1/admin/auth/otp/verify")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"code\":\"000000\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fallbackRequired").value(true));

        when(authenticationService.validateTelegramOtp("LOGIN", "hash-acc", "654321")).thenReturn(true);
        mvc.perform(post("/api/v1/admin/auth/otp/verify")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"code\":\"654321\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("administrator2026-1"));

        mvc.perform(get("/api/v1/admin/auth/session"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authenticated").value(false));

        mvc.perform(get("/api/v1/admin/auth/session").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.username").value("administrator2026-1"));
    }

    @Test
    void enforcesTelegramOtpOnCredentialChangeWhenLinked() throws Exception {
        when(telegramLinkService.status()).thenReturn(new TelegramLinkService.TelegramLinkStatus(true, true));
        when(telegramLinkService.resolveActiveChatId()).thenReturn(Optional.of("999000"));
        when(telegramLinkService.activeAccountHash()).thenReturn(Optional.of("hash-acc"));
        when(authenticationService.issueTelegramOtp("CREDENTIAL_CHANGE", "hash-acc")).thenReturn("112233");
        when(authenticationService.validateTelegramOtp("CREDENTIAL_CHANGE", "hash-acc", "112233")).thenReturn(true);
        AdministratorAccount updated = new AdministratorAccount("administrator2026-2", "h", "a");
        when(authenticationService.changeCredentials(any(), any())).thenReturn(updated);

        mvc.perform(post("/api/v1/admin/account/credentials/otp"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.otpRequired").value(true));
        verify(telegramClient).sendMessage(eq("999000"), contains("112233"));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AdminAuthenticationService.SESSION_USERNAME, "administrator2026-1");
        mvc.perform(patch("/api/v1/admin/account/credentials")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"administrator2026-2\",\"currentPassword\":\"RepresentanteIng2026\",\"newPassword\":\"RepresentanteIng2027\",\"securityAnswer\":\"Ing\",\"otpCode\":\"112233\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("administrator2026-2"));
    }
}
