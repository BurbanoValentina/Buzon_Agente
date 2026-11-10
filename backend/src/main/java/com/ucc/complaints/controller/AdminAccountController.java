package com.ucc.complaints.controller;

import com.ucc.complaints.dto.security.ChangeCredentialsRequest;
import com.ucc.complaints.model.AdministratorAccount;
import com.ucc.complaints.security.AdminAuthenticationService;
import com.ucc.complaints.service.ComplaintValidationException;
import com.ucc.complaints.service.TelegramLinkService;
import com.ucc.complaints.telegram.TelegramChallengeService;
import com.ucc.complaints.telegram.TelegramClient;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/account")
public class AdminAccountController {
    private static final String PURPOSE_CREDENTIAL_CHANGE = "CREDENTIAL_CHANGE";

    private final AdminAuthenticationService authenticationService;
    private TelegramLinkService telegramLinkService;
    private TelegramClient telegramClient;
    private TelegramChallengeService challengeService;

    public AdminAccountController(AdminAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Autowired(required = false)
    public void setTelegramLinkService(TelegramLinkService telegramLinkService) {
        this.telegramLinkService = telegramLinkService;
    }

    @Autowired(required = false)
    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Autowired(required = false)
    public void setChallengeService(TelegramChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping("/security-question")
    public Map<String, String> securityQuestion() {
        return Map.of("question", AdminAuthenticationService.SECURITY_QUESTION);
    }

    @PostMapping("/credentials/otp")
    public ResponseEntity<Map<String, Object>> requestCredentialsOtp() {
        Optional<String> chatId = resolveLinkedChatId();
        if (chatId.isEmpty() || telegramClient == null || challengeService == null) {
            return ResponseEntity.ok(Map.of(
                "otpRequired", false,
                "message", "No hay cuenta Telegram vinculada; puedes guardar directamente."
            ));
        }
        String accountKey = telegramLinkService.activeAccountHash().orElse("admin");
        String otp = authenticationService.issueTelegramOtp(PURPOSE_CREDENTIAL_CHANGE, accountKey);
        try {
            telegramClient.sendMessage(chatId.get(),
                "Código OTP para cambio de credenciales · Buzón UCC: " + otp + "\nVálido durante 15 segundos.");
        } catch (RuntimeException exception) {
            throw new ComplaintValidationException("No fue posible enviar el código OTP por Telegram.");
        }
        return ResponseEntity.ok(Map.of(
            "otpRequired", true,
            "expiresInSeconds", 15,
            "message", "Se envió el código OTP de 15 segundos a tu Telegram."
        ));
    }

    @PatchMapping("/credentials")
    public ResponseEntity<Map<String, String>> changeCredentials(
        @Valid @RequestBody ChangeCredentialsRequest request, HttpSession session) {
        Optional<String> chatId = resolveLinkedChatId();
        if (chatId.isPresent() && challengeService != null) {
            String accountKey = telegramLinkService.activeAccountHash().orElse("admin");
            String otp = request.otpCode() == null ? "" : request.otpCode().trim();
            if (otp.isBlank() || !authenticationService.validateTelegramOtp(PURPOSE_CREDENTIAL_CHANGE, accountKey, otp)) {
                throw new ComplaintValidationException(
                    "El código OTP de Telegram (15s) es obligatorio o expiró.");
            }
        }
        String currentUsername = (String) session.getAttribute(
            AdminAuthenticationService.SESSION_USERNAME);
        AdministratorAccount account = authenticationService.changeCredentials(currentUsername, request);
        session.setAttribute(AdminAuthenticationService.SESSION_USERNAME, account.getUsername());
        session.setMaxInactiveInterval(10 * 60 * 60);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
            account.getUsername(), null, AuthorityUtils.createAuthorityList("ROLE_ADMIN")));
        SecurityContextHolder.setContext(context);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return ResponseEntity.ok(Map.of("username", account.getUsername()));
    }

    private Optional<String> resolveLinkedChatId() {
        if (telegramLinkService == null || !telegramLinkService.status().linked()) {
            return Optional.empty();
        }
        return telegramLinkService.resolveActiveChatId();
    }
}
