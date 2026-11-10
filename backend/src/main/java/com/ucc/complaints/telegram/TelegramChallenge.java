package com.ucc.complaints.telegram;

import java.time.OffsetDateTime;

public record TelegramChallenge(
    String purpose,
    String protectedValueHash,
    OffsetDateTime expiresAt,
    int attemptsRemaining) {
}
