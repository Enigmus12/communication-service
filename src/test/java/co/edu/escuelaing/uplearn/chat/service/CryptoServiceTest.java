package co.edu.escuelaing.uplearn.chat.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceTest {

    @Test
    void encryptDecrypt_roundtripTextoSimple_OK1() {
        CryptoService cs = new CryptoService("secret123");
        cs.initKey();
        String enc = cs.encrypt("hola");
        assertNotEquals("hola", enc);
        assertEquals("hola", cs.decrypt(enc));
    }

    @Test
    void encryptDecrypt_roundtripUnicode_OK2() {
        CryptoService cs = new CryptoService("secret123");
        cs.initKey();
        String enc = cs.encrypt("áé😀漢字");
        assertEquals("áé😀漢字", cs.decrypt(enc));
    }

    @Test
    void encrypt_nullDevuelveNull_FAIL1() {
        CryptoService cs = new CryptoService("secret123");
        cs.initKey();
        assertNull(cs.encrypt(null));
    }

    @Test
    void decrypt_noBase64DevuelveOriginal_FAIL2() {
        CryptoService cs = new CryptoService("secret123");
        cs.initKey();
        assertEquals("not-base64", cs.decrypt("not-base64"));
    }
}
