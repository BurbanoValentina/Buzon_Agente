package com.ucc.complaints.telegram;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpTelegramClientTest {
    @Test
    void sendsMessagesFetchesBotInfoUpdatesAndClearsWebhook() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.telegram.org");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo("https://api.telegram.org/bottoken-test/sendMessage"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.telegram.org/bottoken-test/getMe"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"ok\":true,\"result\":{\"username\":\"ucc_bot\",\"first_name\":\"Buzon\"}}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.telegram.org/bottoken-test/getUpdates?offset=0&timeout=20"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"ok\":true,\"result\":[{\"update_id\":77,\"message\":{\"text\":\"Ayuda\"}}]}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.telegram.org/bottoken-test/deleteWebhook"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        HttpTelegramClient client = new HttpTelegramClient("token-test", "secret-123", true, builder.build());

        client.sendMessage("12345", "Hola UCC");
        Map<String, Object> botInfo = client.getBotInfo();
        assertEquals(true, botInfo.get("enabled"));
        assertEquals("ucc_bot", botInfo.get("username"));
        // Second call uses cache cleanly without extra HTTP call
        assertEquals("ucc_bot", client.getBotInfo().get("username"));

        List<Map<String, Object>> updates = client.fetchUpdates(0L, 20);
        assertEquals(1, updates.size());

        client.clearWebhookForPolling();

        assertTrue(client.acceptsWebhookSecret("secret-123"));
        assertFalse(client.acceptsWebhookSecret("wrong"));
        assertEquals("Ayuda", client.buildUpdate("Ayuda", "1").get("text"));
        server.verify();
    }

    @Test
    void handlesDisabledClientAndHttpErrors() {
        HttpTelegramClient disabled = new HttpTelegramClient("", "", false);
        disabled.sendMessage("1", "msg");
        disabled.clearWebhookForPolling();
        assertEquals(false, disabled.getBotInfo().get("enabled"));
        assertTrue(disabled.fetchUpdates(0, 10).isEmpty());
        assertFalse(disabled.acceptsWebhookSecret("anything"));

        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.telegram.org");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo("https://api.telegram.org/bottoken-test/sendMessage"))
            .andRespond(withServerError());
        server.expect(requestTo("https://api.telegram.org/bottoken-test/getUpdates?offset=5&timeout=10"))
            .andRespond(withServerError());

        HttpTelegramClient failing = new HttpTelegramClient("token-test", "sec", true, builder.build());

        assertThrows(TelegramDeliveryException.class, () -> failing.sendMessage("1", "msg"));
        assertThrows(TelegramDeliveryException.class, () -> failing.fetchUpdates(5L, 10));
        server.verify();
    }
}
