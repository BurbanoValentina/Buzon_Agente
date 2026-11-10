package com.ucc.complaints.controller;

import com.ucc.complaints.service.ComplaintValidationException;
import com.ucc.complaints.service.TelegramLinkService;
import com.ucc.complaints.telegram.TelegramChallengeService;
import com.ucc.complaints.telegram.TelegramClient;
import com.ucc.complaints.telegram.TelegramDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/telegram")
public class TelegramController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramController.class);
    private final TelegramLinkService linkService;
    private final TelegramChallengeService challengeService;
    private TelegramClient telegramClient;

    public TelegramController(TelegramLinkService linkService, TelegramChallengeService challengeService) {
        this.linkService = linkService;
        this.challengeService = challengeService;
    }

    @Autowired(required = false)
    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @GetMapping("/status")
    public TelegramLinkService.TelegramLinkStatus status() {
        return linkService.status();
    }

    @GetMapping("/bot-info")
    public Map<String, Object> botInfo() {
        if (telegramClient == null) {
            return Map.of("enabled", false, "username", "", "firstName", "");
        }
        return telegramClient.getBotInfo();
    }

    @PostMapping("/link")
    public ResponseEntity<Map<String, String>> issueLink(@RequestBody Map<String, String> request) {
        String accountId = request.getOrDefault("accountId", "").trim();
        if (accountId.isBlank()) {
            throw new ComplaintValidationException("Ingresa tu ID numérico de chat de Telegram.");
        }
        if (!accountId.matches("^-?\\d+$")) {
            throw new ComplaintValidationException(
                "Ingresa únicamente tu ID numérico de chat (solo números, tal como te lo respondió /start en Telegram).");
        }
        String accountHash = challengeService.hash(accountId);
        String code = challengeService.issueLinkCode(accountHash, "LINK");
        LOGGER.info("Telegram link code generated (telegramClientPresent={})", telegramClient != null);
        if (telegramClient != null) {
            try {
                telegramClient.sendMessage(accountId,
                    "Código de vinculación · Buzón UCC: " + code
                        + "\nVálido durante 10 minutos. Ingrésalo en el panel administrativo para completar la conexión.");
            } catch (TelegramDeliveryException exception) {
                LOGGER.warn("Telegram link code delivery failed: {}", exception.getMessage());
                throw new ComplaintValidationException(
                    "No pudimos enviarte el código por Telegram. Abre el bot primero, envíale /start y verifica usar tu ID numérico.");
            }
        }
        return ResponseEntity.ok(Map.of(
            "challengeExpiresInSeconds", "600",
            "delivered", Boolean.toString(telegramClient != null)
        ));
    }

    @PostMapping("/confirm-link")
    public ResponseEntity<?> confirmLink(
        @RequestBody Map<String, String> request) {
        String accountId = request.getOrDefault("accountId", "").trim();
        String code = request.getOrDefault("code", "").trim();
        if (!accountId.matches("^-?\\d+$")) {
            return ResponseEntity.badRequest().body(Map.of(
                "linked", false,
                "notificationsEnabled", false,
                "message", "Ingresa primero tu ID numérico de chat (solo números, tal como te lo respondió /start)."
            ));
        }
        if (code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "linked", false,
                "notificationsEnabled", false,
                "message", "Ingresa el código temporal de 6 dígitos recibido en Telegram."
            ));
        }
        String accountHash = challengeService.hash(accountId);
        if (!challengeService.validate("LINK", accountHash, code)) {
            return ResponseEntity.badRequest().body(Map.of(
                "linked", false,
                "notificationsEnabled", false,
                "message", "El código de vinculación es inválido o expiró. Solicita un nuevo código."
            ));
        }
        TelegramLinkService.TelegramLinkStatus linkedStatus = linkService.link(accountHash, accountId);
        if (linkedStatus == null) {
            linkedStatus = linkService.link(accountHash);
        }
        if (telegramClient != null && !accountId.isBlank()) {
            try {
                telegramClient.sendMessage(accountId,
                    "Vinculación confirmada · Buzón UCC.\n"
                        + "A partir de ahora recibirás códigos OTP (15s) al iniciar sesión o cambiar tu contraseña, y alertas de nuevas quejas según tu configuración.");
            } catch (RuntimeException ignored) {
                // Non-fatal confirmation message failure
            }
        }
        return ResponseEntity.ok(linkedStatus);
    }

    @PostMapping("/unlink")
    public TelegramLinkService.TelegramLinkStatus unlink() {
        return linkService.unlink();
    }

    @PatchMapping("/notifications")
    public TelegramLinkService.TelegramLinkStatus notifications(@RequestBody Map<String, Boolean> request) {
        return linkService.setNotifications(Boolean.TRUE.equals(request.get("enabled")));
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> sendTestNotification() {
        Optional<String> chatId = linkService.resolveActiveChatId();
        if (chatId.isEmpty() || telegramClient == null) {
            throw new ComplaintValidationException("Vincula primero una cuenta de Telegram con ID numérico válido.");
        }
        try {
            telegramClient.sendMessage(chatId.get(),
                "Prueba de conexión exitosa · Buzón UCC.\nTu bot administrativo está activo y listo.");
            return ResponseEntity.ok(Map.of("message", "Mensaje de prueba enviado a tu Telegram."));
        } catch (TelegramDeliveryException exception) {
            throw new ComplaintValidationException("No fue posible enviar la prueba por Telegram en este momento.");
        }
    }
}
