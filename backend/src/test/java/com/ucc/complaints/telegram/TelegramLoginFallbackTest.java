package com.ucc.complaints.telegram;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TelegramLoginFallbackTest {
    @Test
    void threeWrongCodesActivateSecurityFallback() {
        TelegramChallengeService service = new TelegramChallengeService();
        service.issueLoginOtp("LOGIN", "account");
        service.validate("LOGIN", "account", "000000");
        service.validate("LOGIN", "account", "000001");
        service.validate("LOGIN", "account", "000002");
        assertTrue(service.requiresSecurityFallback("account"));
    }
}
