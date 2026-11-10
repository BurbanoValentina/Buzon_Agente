package com.ucc.complaints.telegram;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelegramChallengeService {
    private final SecureRandom random = new SecureRandom();
    private final Map<String, TelegramChallenge> challenges = new ConcurrentHashMap<>();
    private final Map<String, Integer> wrongCodes = new ConcurrentHashMap<>();

    public String issueLinkCode(String accountId, String purpose) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        TelegramChallenge challenge = new TelegramChallenge(purpose,
            hash(code), OffsetDateTime.now().plusMinutes(10), 3);
        challenges.put(purpose + ":" + accountId, challenge);
        return code;
    }

    public String issueLoginOtp(String purpose, String accountId) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        TelegramChallenge challenge = new TelegramChallenge(purpose,
            hash(code), OffsetDateTime.now().plusSeconds(15), 3);
        challenges.put(purpose + ":" + accountId, challenge);
        return code;
    }

    public boolean validate(String purpose, String accountId, String presentedCode) {
        String key = purpose + ":" + accountId;
        TelegramChallenge challenge = challenges.get(key);
        if (challenge == null || challenge.expiresAt().isBefore(OffsetDateTime.now())) {
            wrongCodes.merge(key, 1, Integer::sum);
            if (wrongCodes.getOrDefault(key, 0) >= 3) {
                challenges.remove(key);
            }
            return false;
        }
        if (!challenge.protectedValueHash().equals(hash(presentedCode == null ? "" : presentedCode.trim()))) {
            int attempts = wrongCodes.merge(key, 1, Integer::sum);
            if (attempts >= 3) {
                challenges.remove(key);
            }
            return false;
        }
        challenges.remove(key);
        wrongCodes.remove(key);
        return true;
    }

    public boolean requiresSecurityFallback(String accountId) {
        return wrongCodes.getOrDefault("LOGIN:" + accountId, 0) >= 3;
    }

    public int wrongAttempts(String purpose, String accountId) {
        return wrongCodes.getOrDefault(purpose + ":" + accountId, 0);
    }

    public void clearLoginFallback(String accountId) {
        if (accountId != null) {
            wrongCodes.remove("LOGIN:" + accountId);
            challenges.remove("LOGIN:" + accountId);
        }
    }

    public String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("No fue posible proteger el código.", exception);
        }
    }
}
