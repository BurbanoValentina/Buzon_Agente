package com.ucc.complaints.controller;

import com.ucc.complaints.service.TelegramCommandService;
import com.ucc.complaints.service.TelegramLinkService;
import com.ucc.complaints.telegram.TelegramClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TelegramWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
class TelegramWebhookControllerFullTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private TelegramClient client;

    @MockBean
    private TelegramCommandService commandService;

    @MockBean
    private TelegramLinkService linkService;

    @Test
    void rejectsInvalidSecretToken() throws Exception {
        when(client.acceptsWebhookSecret("wrong")).thenReturn(false);

        mvc.perform(post("/api/v1/public/telegram/webhook")
            .header("X-Telegram-Bot-Api-Secret-Token", "wrong")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void processesUpdateAndExecutesCommand() throws Exception {
        when(client.acceptsWebhookSecret("secret123")).thenReturn(true);
        when(linkService.status()).thenReturn(new TelegramLinkService.TelegramLinkStatus(true, true));
        when(commandService.execute(any(), any())).thenReturn("Respuesta del bot");

        String payload = """
            {
                "message": {
                    "text": "/resumen",
                    "from": {
                        "id": "123456789"
                    }
                }
            }
            """;

        mvc.perform(post("/api/v1/public/telegram/webhook")
            .header("X-Telegram-Bot-Api-Secret-Token", "secret123")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isOk());

        verify(client).sendMessage("123456789", "Respuesta del bot");
    }
}
