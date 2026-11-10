package com.ucc.complaints.telegram;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TelegramChallengeServiceTest {
    @Test
    void codeIsProtectedAndSingleUse() {
        TelegramChallengeService service = new TelegramChallengeService();
        String code = service.issueLoginOtp("OTP", "account");
        assertFalse(code.isBlank());
        assertNotEquals(code, service.hash(code));
        assertTrue(service.validate("OTP", "account", code));
        assertFalse(service.validate("OTP", "account", code));
    }
}
