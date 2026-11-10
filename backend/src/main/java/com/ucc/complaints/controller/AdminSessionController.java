package com.ucc.complaints.controller;

import com.ucc.complaints.dto.security.*;
import com.ucc.complaints.model.AdministratorAccount;
import com.ucc.complaints.security.AdminAuthenticationService;
import com.ucc.complaints.service.ComplaintValidationException;
import com.ucc.complaints.service.TelegramLinkService;
import com.ucc.complaints.telegram.TelegramChallengeService;
import com.ucc.complaints.telegram.TelegramClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminSessionController {
    private static final String PENDING_OTP_USER = "PENDING_OTP_USER";
    private static final String PENDING_OTP_KEY = "PENDING_OTP_KEY";

    private final AdminAuthenticationService authenticationService;
    private TelegramLinkService telegramLinkService;
    private TelegramClient telegramClient;
    private TelegramChallengeService challengeService;

    public AdminSessionController(AdminAuthenticationService authenticationService) {
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

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AdminLoginRequest request,
                                                     HttpSession session) {
        AdministratorAccount account = authenticationService.authenticate(request);
        Optional<String> linkedChatId = resolveLinkedChatId();
        if (linkedChatId.isPresent() && telegramClient != null && challengeService != null) {
            String accountKey = telegramLinkService.activeAccountHash().orElse("admin");
            challengeService.clearLoginFallback(accountKey);
            String otp = authenticationService.issueTelegramOtp("LOGIN", accountKey);
            try {
                telegramClient.sendMessage(linkedChatId.get(),
                    "Código OTP de acceso · Buzón UCC: " + otp + "\nVálido durante 15 segundos.");
            } catch (RuntimeException exception) {
                throw new ComplaintValidationException(
                    "No fue posible enviar el código OTP por Telegram. Verifica la conexión o usa tu pregunta clave.");
            }
            session.setAttribute(PENDING_OTP_USER, account.getUsername());
            session.setAttribute(PENDING_OTP_KEY, accountKey);
            return ResponseEntity.ok(Map.of(
                "username", account.getUsername(),
                "otpRequired", true,
                "expiresInSeconds", 15,
                "message", "Código enviado a tu Telegram. Expira en 15 segundos."
            ));
        }

        establishAuthenticatedSession(session, account.getUsername());
        return ResponseEntity.ok(Map.of(
            "username", account.getUsername(),
            "otpRequired", false
        ));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request,
                                                         HttpSession session) {
        String pendingUser = (String) session.getAttribute(PENDING_OTP_USER);
        String pendingKey = (String) session.getAttribute(PENDING_OTP_KEY);
        if (pendingUser == null || pendingKey == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "No hay una verificación pendiente. Inicia sesión nuevamente."));
        }
        String code = request.getOrDefault("code", "").trim();
        if (authenticationService.validateTelegramOtp("LOGIN", pendingKey, code)) {
            session.removeAttribute(PENDING_OTP_USER);
            session.removeAttribute(PENDING_OTP_KEY);
            establishAuthenticatedSession(session, pendingUser);
            return ResponseEntity.ok(Map.of(
                "username", pendingUser,
                "otpRequired", false
            ));
        }

        boolean fallbackRequired = challengeService != null && challengeService.requiresSecurityFallback(pendingKey);
        if (fallbackRequired) {
            authenticationService.recordFallbackActivated(pendingUser);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "fallbackRequired", true,
                "question", AdminAuthenticationService.SECURITY_QUESTION,
                "message", "Se agotaron los 3 intentos del código OTP. Responde la pregunta clave para acceder."
            ));
        }
        int used = challengeService == null ? 1 : challengeService.wrongAttempts("LOGIN", pendingKey);
        int remaining = Math.max(0, 3 - used);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "fallbackRequired", false,
            "remainingAttempts", remaining,
            "message", "Código OTP inválido o expirado. Te quedan " + remaining + " intento(s)."
        ));
    }

    @PostMapping("/otp/resend")
    public ResponseEntity<Map<String, Object>> resendOtp(HttpSession session) {
        String pendingUser = (String) session.getAttribute(PENDING_OTP_USER);
        String pendingKey = (String) session.getAttribute(PENDING_OTP_KEY);
        Optional<String> linkedChatId = resolveLinkedChatId();
        if (pendingUser == null || pendingKey == null || linkedChatId.isEmpty() || telegramClient == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "No hay una sesión pendiente para reenviar el código."));
        }
        String otp = authenticationService.issueTelegramOtp("LOGIN", pendingKey);
        try {
            telegramClient.sendMessage(linkedChatId.get(),
                "Nuevo código OTP · Buzón UCC: " + otp + "\nVálido durante 15 segundos.");
        } catch (RuntimeException exception) {
            throw new ComplaintValidationException("No fue posible reenviar el código por Telegram.");
        }
        return ResponseEntity.ok(Map.of(
            "otpRequired", true,
            "expiresInSeconds", 15,
            "message", "Se envió un nuevo código de 15 segundos a tu Telegram."
        ));
    }

    @PostMapping("/security-question")
    public ResponseEntity<Map<String, String>> securityQuestion(
        @Valid @RequestBody SecurityQuestionRequest request, HttpSession session) {
        AdministratorAccount account = authenticationService.recover(request);
        Object pendingKey = session.getAttribute(PENDING_OTP_KEY);
        if (challengeService != null && pendingKey instanceof String keyValue) {
            challengeService.clearLoginFallback(keyValue);
        }
        session.removeAttribute(PENDING_OTP_USER);
        session.removeAttribute(PENDING_OTP_KEY);
        establishAuthenticatedSession(session, account.getUsername());
        return ResponseEntity.ok(Map.of(
            "username", account.getUsername(),
            "question", AdminAuthenticationService.SECURITY_QUESTION
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> currentSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object username = session.getAttribute(AdminAuthenticationService.SESSION_USERNAME);
            if (username instanceof String value && !value.isBlank()) {
                return ResponseEntity.ok(Map.of("authenticated", true, "username", value));
            }
        }
        return ResponseEntity.ok(Map.of("authenticated", false));
    }

    @GetMapping("/session-expired")
    public ResponseEntity<Map<String, String>> sessionExpired() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", "La sesión expiró por inactividad."));
    }

    private void establishAuthenticatedSession(HttpSession session, String username) {
        HttpSession targetSession = session;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest servletRequest = attrs.getRequest();
            if (servletRequest.getSession(false) != null) {
                try {
                    servletRequest.changeSessionId();
                    HttpSession rotated = servletRequest.getSession(false);
                    if (rotated != null) {
                        targetSession = rotated;
                    }
                } catch (IllegalStateException ignored) {
                    // Mock or test request without active servlet session rotation support
                }
            }
        }
        targetSession.setAttribute(AdminAuthenticationService.SESSION_USERNAME, username);
        targetSession.setMaxInactiveInterval(10 * 60 * 60);
        var authentication = new UsernamePasswordAuthenticationToken(
            username, null, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        targetSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    private Optional<String> resolveLinkedChatId() {
        if (telegramLinkService == null || !telegramLinkService.status().linked()) {
            return Optional.empty();
        }
        return telegramLinkService.resolveActiveChatId();
    }
}
