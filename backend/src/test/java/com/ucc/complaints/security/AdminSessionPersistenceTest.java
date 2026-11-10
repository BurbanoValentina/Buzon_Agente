package com.ucc.complaints.security;

import com.ucc.complaints.controller.AdminAccountController;
import com.ucc.complaints.controller.AdminSessionController;
import com.ucc.complaints.controller.TelegramController;
import com.ucc.complaints.controller.WordReportController;
import com.ucc.complaints.dto.report.WordReportDraftResponse;
import com.ucc.complaints.model.AdministratorAccount;
import com.ucc.complaints.service.AuditService;
import com.ucc.complaints.service.TelegramLinkService;
import com.ucc.complaints.service.WordReportService;
import com.ucc.complaints.telegram.TelegramChallengeService;
import com.ucc.complaints.telegram.TelegramClient;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({
    AdminSessionController.class,
    AdminAccountController.class,
    TelegramController.class,
    WordReportController.class
})
@Import({SecurityConfig.class, AdminSessionAuthenticationFilter.class})
class AdminSessionPersistenceTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private AdminAuthenticationService authenticationService;

    @MockBean
    private AuditService auditService;

    @MockBean
    private TelegramLinkService telegramLinkService;

    @MockBean
    private TelegramChallengeService challengeService;

    @MockBean
    private TelegramClient telegramClient;

    @MockBean
    private WordReportService wordReportService;

    @Test
    void keepsSessionAuthenticatedAcrossAllAdminSectionsWithoutRotatingIdMidFlight() throws Exception {
        AdministratorAccount account = new AdministratorAccount("administrator2026-1", "hash", "ans");
        when(authenticationService.authenticate(any())).thenReturn(account);
        when(telegramLinkService.status()).thenReturn(new TelegramLinkService.TelegramLinkStatus(false, false));
        when(telegramClient.getBotInfo()).thenReturn(Map.of("enabled", true, "username", "Buzonucc_bot", "firstName", "Buzbot"));
        OffsetDateTime now = OffsetDateTime.now();
        when(wordReportService.create(any(), any())).thenReturn(
            new WordReportDraftResponse(1L, "Reporte", "Contenido", now, now));

        // 1. Unauthenticated admin request still rejected with 403
        mvc.perform(get("/api/v1/admin/telegram/status"))
            .andExpect(status().isForbidden());

        // 2. Stale JSESSIONID on session probe must not redirect to /session-expired
        mvc.perform(get("/api/v1/admin/auth/session").cookie(new Cookie("JSESSIONID", "STALE_ID")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authenticated").value(false));

        // 3. Login stores SecurityContext & ADMIN_USERNAME directly in HttpSession
        MvcResult loginResult = mvc.perform(post("/api/v1/admin/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"administrator2026-1\",\"password\":\"RepresentanteIng2026\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("administrator2026-1"))
            .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);
        assertEquals("administrator2026-1", session.getAttribute(AdminAuthenticationService.SESSION_USERNAME));
        assertNotNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
        String sessionIdAfterLogin = session.getId();

        // 4. Subsequent navigation across Word, Telegram, Account, and Session stays 200 OK with same session ID
        mvc.perform(get("/api/v1/admin/auth/session").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authenticated").value(true));

        mvc.perform(post("/api/v1/admin/reports/word/drafts").session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"complaintIds\":[],\"category\":\"\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));

        mvc.perform(get("/api/v1/admin/telegram/status").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.linked").value(false));

        mvc.perform(get("/api/v1/admin/telegram/bot-info").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("Buzonucc_bot"));

        mvc.perform(get("/api/v1/admin/account/security-question").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.question").value(AdminAuthenticationService.SECURITY_QUESTION));

        assertEquals(sessionIdAfterLogin, session.getId());
    }
}
