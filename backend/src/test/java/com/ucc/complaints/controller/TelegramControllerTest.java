package com.ucc.complaints.controller;

import com.ucc.complaints.service.TelegramLinkService;
import com.ucc.complaints.telegram.TelegramChallengeService;
import com.ucc.complaints.telegram.TelegramClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TelegramController.class)
@AutoConfigureMockMvc(addFilters = false)
class TelegramControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private TelegramLinkService linkService;

    @MockBean
    private TelegramChallengeService challengeService;

    @MockBean
    private TelegramClient telegramClient;

    @Test
    void testTelegramEndpointsAndRealDelivery() throws Exception {
        when(linkService.status()).thenReturn(new TelegramLinkService.TelegramLinkStatus(true, true));
        when(linkService.resolveActiveChatId()).thenReturn(Optional.of("12345"));
        when(challengeService.hash(any())).thenReturn("hashed");
        when(challengeService.issueLinkCode(any(), any())).thenReturn("code123");
        when(challengeService.validate(any(), any(), any())).thenReturn(true);
        when(linkService.link(any(), any())).thenReturn(new TelegramLinkService.TelegramLinkStatus(true, true));
        when(linkService.unlink()).thenReturn(new TelegramLinkService.TelegramLinkStatus(false, false));
        when(linkService.setNotifications(anyBoolean())).thenReturn(new TelegramLinkService.TelegramLinkStatus(true, true));
        when(telegramClient.getBotInfo()).thenReturn(Map.of("enabled", true, "username", "ucc_bot", "firstName", "Buzon UCC"));

        mvc.perform(get("/api/v1/admin/telegram/status")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/telegram/bot-info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("ucc_bot"));
        mvc.perform(post("/api/v1/admin/telegram/link")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"@usuario\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ID numérico")));

        mvc.perform(post("/api/v1/admin/telegram/link")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"12345\"}"))
            .andExpect(status().isOk());
        verify(telegramClient).sendMessage(eq("12345"), contains("code123"));

        when(challengeService.validate("LINK", "hashed", "000000")).thenReturn(false);
        mvc.perform(post("/api/v1/admin/telegram/confirm-link")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"@bad\",\"code\":\"123456\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.linked").value(false))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ID numérico")));

        mvc.perform(post("/api/v1/admin/telegram/confirm-link")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"12345\",\"code\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.linked").value(false));

        mvc.perform(post("/api/v1/admin/telegram/confirm-link")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"12345\",\"code\":\"000000\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.linked").value(false))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("inválido o expiró")));

        when(challengeService.validate("LINK", "hashed", "code123")).thenReturn(true);
        mvc.perform(post("/api/v1/admin/telegram/confirm-link")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"12345\",\"code\":\"code123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.linked").value(true));

        mvc.perform(post("/api/v1/admin/telegram/test")).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/admin/telegram/notifications")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"enabled\":true}"))
            .andExpect(status().isOk());
        mvc.perform(post("/api/v1/admin/telegram/unlink")).andExpect(status().isOk());
    }
}
