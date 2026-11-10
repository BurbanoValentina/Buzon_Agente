package com.ucc.complaints.telegram;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelegramCryptoServiceTest {
    @Test
    void encryptsAndDecryptsChatIdWithUniqueIvs() {
        TelegramCryptoService crypto = new TelegramCryptoService("secret-test-material");
        String first = crypto.encrypt("987654321");
        String second = crypto.encrypt("987654321");

        assertNotNull(first);
        assertNotEquals("987654321", first);
        assertNotEquals(first, second);
        assertEquals("987654321", crypto.decrypt(first).orElseThrow());
        assertEquals("987654321", crypto.decrypt(second).orElseThrow());
    }

    @Test
    void handlesBlankAndInvalidCipherTextGracefully() {
        TelegramCryptoService crypto = new TelegramCryptoService("");
        assertNull(crypto.encrypt(null));
        assertNull(crypto.encrypt("   "));
        assertTrue(crypto.decrypt(null).isEmpty());
        assertTrue(crypto.decrypt("not-valid-base64!").isEmpty());
        assertTrue(crypto.decrypt("AAAA").isEmpty());
    }
}
