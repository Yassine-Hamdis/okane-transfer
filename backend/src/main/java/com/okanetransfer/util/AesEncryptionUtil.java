package com.okanetransfer.util;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encryption utility for encrypting sensitive fields at rest.
 *
 * <p>Used to encrypt identity document numbers (CIN, passport) before persisting
 * them to the database, as required by the OkaneTransfer security specifications.</p>
 *
 * <p><b>Algorithm:</b> AES/GCM/NoPadding — 256-bit key, 96-bit IV, 128-bit auth tag.<br>
 * GCM is preferred over CBC because it provides both <i>confidentiality</i> and
 * <i>integrity</i>: any tampering with the ciphertext is detected at decryption time.</p>
 *
 * <p><b>Output format</b> (Base64-encoded): {@code [ IV (12 bytes) | Ciphertext + AuthTag ]}</p>
 *
 * <p><b>Key setup — run once:</b></p>
 * <pre>{@code
 *   // 1. Generate a key
 *   String key = AesEncryptionUtil.generateBase64Key();
 *
 *   // 2. Export as environment variable — NEVER paste into application.properties directly
 *   //    Linux/Mac:  export AES_SECRET_KEY=<value>
 *   //    Docker:     environment: - AES_SECRET_KEY=<value>
 *   //    GitHub Actions secret: AES_SECRET_KEY → ${{ secrets.AES_SECRET_KEY }}
 * }</pre>
 *
 * <p><b>application.properties</b> (safe to commit — no secret value here):</p>
 * <pre>
 *   security.aes.key=${AES_SECRET_KEY}
 * </pre>
 *
 * <p><b>Usage in a Spring service:</b></p>
 * <pre>{@code
 *   @Autowired
 *   private AesEncryptionUtil aesEncryptionUtil;
 *
 *   String encrypted = aesEncryptionUtil.encrypt("AB123456");
 *   String decrypted = aesEncryptionUtil.decrypt(encrypted); // "AB123456"
 * }</pre>
 */
@Component
public class AesEncryptionUtil {

    // -----------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(AesEncryptionUtil.class);

    private static final String ALGORITHM         = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM     = "AES";
    private static final int    GCM_IV_LENGTH     = 12;   // 96-bit IV — NIST recommended for GCM
    private static final int    GCM_TAG_LENGTH    = 128;  // 128-bit authentication tag
    private static final int    REQUIRED_KEY_BYTES = 32;  // 256-bit key

    // -----------------------------------------------------------------
    // Spring wiring
    // -----------------------------------------------------------------

    /**
     * Injected from application.properties: {@code security.aes.key=${AES_SECRET_KEY}}
     * The actual value comes from the environment variable AES_SECRET_KEY.
     */
    @Value("${aes.secret.key}")
    private String base64Key;

    private SecretKey secretKey;

    /**
     * Initialises the SecretKey after Spring has injected {@code base64Key}.
     * Fails fast at startup if the key is missing, blank, or the wrong length —
     * so misconfiguration is caught before any request is served.
     */
    @PostConstruct
    public void init() {
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalStateException(
                    "[OkaneTransfer] AES key (security.aes.key / AES_SECRET_KEY) is not configured."
            );
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Key);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "[OkaneTransfer] AES key is not valid Base64. Re-generate with AesEncryptionUtil.generateBase64Key().", e
            );
        }

        if (keyBytes.length != REQUIRED_KEY_BYTES) {
            throw new IllegalStateException(
                    "[OkaneTransfer] AES key must be exactly 32 bytes (256 bits). Got: "
                            + keyBytes.length + " bytes. Re-generate with AesEncryptionUtil.generateBase64Key()."
            );
        }

        this.secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    // -----------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------

    /**
     * Encrypts a plaintext string using AES-256-GCM.
     *
     * <p>A fresh cryptographically random 96-bit IV is generated for every call,
     * ensuring that the same plaintext never produces the same ciphertext.</p>
     *
     * <p>Passing {@code null} returns {@code null} — safe for optional JPA fields.</p>
     *
     * @param plaintext the value to encrypt (e.g., a CIN or passport number)
     * @return Base64-encoded string {@code [IV | ciphertext + authTag]}, or {@code null} if input is {@code null}
     * @throws EncryptionException if the JCE encryption fails
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        try {
            byte[] iv = generateIv();

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Layout: [ IV (12 bytes) | ciphertext + GCM auth tag ]
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);

            return Base64.getEncoder().encodeToString(buffer.array());

        } catch (Exception e) {
            log.error("AES encryption failed", e);
            throw new EncryptionException("Encryption failed.", e);
        }
    }

    /**
     * Decrypts a Base64-encoded AES-256-GCM ciphertext back to plaintext.
     *
     * <p>The GCM authentication tag is verified automatically — if the ciphertext
     * has been tampered with, a {@link EncryptionException} is thrown.</p>
     *
     * <p>Passing {@code null} returns {@code null} — safe for optional JPA fields.</p>
     *
     * @param encryptedBase64 the Base64-encoded value produced by {@link #encrypt(String)}
     * @return the original plaintext string, or {@code null} if input is {@code null}
     * @throws EncryptionException if decryption fails or authentication tag is invalid
     */
    public String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);

            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);

            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("AES decryption failed", e);
            throw new EncryptionException(
                    "Decryption failed. Data may be corrupted or the key may have changed.", e
            );
        }
    }

    // -----------------------------------------------------------------
    // Static utility
    // -----------------------------------------------------------------

    /**
     * Generates a cryptographically secure random 256-bit AES key encoded as Base64.
     *
     * <p>Run this <b>once</b> to produce your key, then store the output as the
     * environment variable {@code AES_SECRET_KEY}. Never hardcode the result
     * in source files or {@code application.properties}.</p>
     *
     * <pre>{@code
     *   // In a main() or a @Bean initializer, run once:
     *   System.out.println(AesEncryptionUtil.generateBase64Key());
     * }</pre>
     *
     * @return Base64-encoded 32-byte (256-bit) AES key
     */
    public static String generateBase64Key() {
        byte[] key = new byte[REQUIRED_KEY_BYTES];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    // -----------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------

    /**
     * Generates a cryptographically secure random 96-bit IV.
     */
    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    // -----------------------------------------------------------------
    // Exception
    // -----------------------------------------------------------------

    /**
     * Unchecked exception wrapping low-level JCE cryptographic errors.
     * Thrown by {@link #encrypt(String)} and {@link #decrypt(String)}.
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}