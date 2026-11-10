package com.ucc.complaints.telegram;

import com.ucc.complaints.service.TelegramCommandService;
import com.ucc.complaints.service.TelegramLinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramLongPollingServiceTest {
    @Mock
    private TelegramClient telegramClient;
    @Mock
    private TelegramCommandService commandService;
    @Mock
    private TelegramLinkService linkService;

    private TelegramChallengeService challengeService;
    private TelegramLongPollingService pollingService;

    @BeforeEach
    void setUp() {
        challengeService = new TelegramChallengeService();
        pollingService = new TelegramLongPollingService(
            telegramClient, commandService, linkService, challengeService,
            false, false, "");
    }

    @Test
    void respondsWithNumericChatIdOnStartWhenUnlinked() {
        when(linkService.activeAccountHash()).thenReturn(Optional.empty());
        Map<String, Object> update = Map.of(
            "update_id", 10,
            "message", Map.of(
                "chat", Map.of("id", 777888999L),
                "text", "/start"
            )
        );

        pollingService.processUpdate(update);

        verify(telegramClient).sendMessage(eq("777888999"), contains("777888999"));
        verifyNoInteractions(commandService);
    }

    @Test
    void respondsWithCommandsOnStartWhenAlreadyLinkedHere() {
        String chatHash = challengeService.hash("777888999");
        when(linkService.activeAccountHash()).thenReturn(Optional.of(chatHash));
        Map<String, Object> update = Map.of(
            "update_id", 11,
            "message", Map.of(
                "chat", Map.of("id", 777888999L),
                "text", "/start"
            )
        );

        pollingService.processUpdate(update);

        verify(telegramClient).sendMessage(eq("777888999"), contains("Resumen"));
    }

    @Test
    void delegatesReadOnlyCommandsToCommandService() {
        String chatHash = challengeService.hash("555111");
        when(commandService.execute(chatHash, "Resumen")).thenReturn("Resumen: 4 quejas registradas.");
        Map<String, Object> update = Map.of(
            "update_id", 12,
            "message", Map.of(
                "from", Map.of("id", 555111L),
                "text", "Resumen"
            )
        );

        pollingService.processUpdate(update);

        verify(telegramClient).sendMessage("555111", "Resumen: 4 quejas registradas.");
    }

    @Test
    void pollOnceAdvancesOffsetAndStartsStopsCleanly() {
        when(commandService.execute(any(), eq("Ayuda"))).thenReturn("Comandos disponibles.");
        when(telegramClient.fetchUpdates(0L, 20)).thenReturn(List.of(
            Map.of("update_id", "42", "message", Map.of("chat", Map.of("id", 111L), "text", "Ayuda"))
        ));

        pollingService.pollOnce();
        verify(telegramClient).sendMessage("111", "Comandos disponibles.");

        pollingService.start();
        pollingService.stop();

        TelegramLongPollingService enabledService = new TelegramLongPollingService(
            telegramClient, commandService, linkService, challengeService,
            true, true, "token");
        enabledService.start();
        enabledService.stop();
    }

    @Test
    void ignoresMalformedOrEmptyUpdatesSafely() {
        pollingService.processUpdate(null);
        pollingService.processUpdate(Map.of());
        pollingService.processUpdate(Map.of("message", Map.of("text", "")));
        verifyNoInteractions(telegramClient, commandService);
    }
}
