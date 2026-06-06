package com.okanetransfer.util;

import com.okanetransfer.repository.TransferRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Generator for unique, cryptographically secure withdrawal codes.
 *
 * <p>Each code is an 8-character alphanumeric string drawn from a 32-character
 * alphabet (uppercase letters + digits), deliberately excluding visually ambiguous
 * characters ({@code 0, O, 1, I}) to reduce transcription errors at the agent counter
 * when codes are read aloud or handwritten.</p>
 *
 * <p>As per the OkaneTransfer CDC spec, the withdrawal code is:</p>
 * <ul>
 *   <li>Generated after payment confirmation by the sending agent</li>
 *   <li>Communicated to the sender (printed receipt / SMS / email)</li>
 *   <li>Presented by the beneficiary at the destination agency to collect funds</li>
 *   <li>Verified against status: EN_ATTENTE / PAYÉ / ANNULÉ / EXPIRÉ</li>
 * </ul>
 *
 * <p><b>Uniqueness guarantee:</b><br>
 * After generating a code, the DB is checked before returning it.
 * If a collision occurs (extremely rare), generation is retried automatically.
 * Max retries = 10 — far more than enough given the collision probability.</p>
 *
 * <p><b>Collision probability:</b><br>
 * Alphabet = 32 chars, length = 8 → 32⁸ ≈ 1 trillion combinations.<br>
 * At 1 million active transfers, P(collision) ≈ 0.000001%.</p>
 *
 * <p><b>application.properties</b> (optional overrides):</p>
 * <pre>
 *   withdrawal.code.length=8
 *   withdrawal.code.validity-hours=48
 * </pre>
 *
 * <p><b>Usage in TransferService:</b></p>
 * <pre>{@code
 *   @Autowired
 *   private WithdrawalCodeGenerator withdrawalCodeGenerator;
 *
 *   WithdrawalCode bundle = withdrawalCodeGenerator.generate();
 *   transfer.setWithdrawalCode(bundle.getCode());
 *   transfer.setWithdrawalCodeExpiresAt(bundle.getExpiresAt());
 * }</pre>
 */
@Component
public class WithdrawalCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(WithdrawalCodeGenerator.class);

    // -----------------------------------------------------------------
    // Alphabet — 32 characters, visually unambiguous
    // Excluded: 0 (zero), O (letter), 1 (one), I (letter)
    // -----------------------------------------------------------------
    private static final String ALPHABET    = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int    MAX_RETRIES = 10;

    // -----------------------------------------------------------------
    // Configurable properties (with safe defaults)
    // -----------------------------------------------------------------

    @Value("${withdrawal.code.length:8}")
    private int codeLength;

    @Value("${withdrawal.code.validity-hours:48}")
    private long validityHours;

    // -----------------------------------------------------------------
    // Dependencies
    // -----------------------------------------------------------------

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private TransferRepository transferRepository;

    // -----------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------

    /**
     * Validates configuration after Spring injection.
     * Fails fast at startup if properties are misconfigured.
     */
    @PostConstruct
    public void init() {
        if (codeLength < 6 || codeLength > 16) {
            throw new IllegalStateException(
                    "[OkaneTransfer] withdrawal.code.length must be between 6 and 16. Got: " + codeLength
            );
        }
        if (validityHours <= 0) {
            throw new IllegalStateException(
                    "[OkaneTransfer] withdrawal.code.validity-hours must be positive. Got: " + validityHours
            );
        }
        log.info("WithdrawalCodeGenerator ready — length={}, validityHours={}", codeLength, validityHours);
    }

    // -----------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------

    /**
     * Generates a unique withdrawal code that does not exist in the DB,
     * wrapped in a {@link WithdrawalCode} bundle with its expiry timestamp.
     *
     * <p>Uniqueness is guaranteed by checking {@code transferRepository.existsByWithdrawalCode()}
     * before returning. In the astronomically unlikely case of a collision,
     * generation is retried up to {@code MAX_RETRIES} times.</p>
     *
     * @return a {@link WithdrawalCode} containing a unique code and expiry time
     * @throws IllegalStateException if a unique code cannot be generated after max retries
     */
    public WithdrawalCode generate() {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            String code = generateRaw();

            if (!transferRepository.existsByWithdrawalCode(code)) {
                log.debug("Withdrawal code generated in {} attempt(s)", attempt);
                return new WithdrawalCode(code, LocalDateTime.now().plusHours(validityHours));
            }

            log.warn("Withdrawal code collision on attempt {}: {}", attempt, code);
        }

        throw new IllegalStateException(
                "[OkaneTransfer] Failed to generate a unique withdrawal code after "
                        + MAX_RETRIES + " attempts. This should never happen."
        );
    }

    /**
     * Validates the format of a withdrawal code against the configured length and alphabet.
     *
     * <p>Does <b>not</b> check database existence, expiry, or payment status —
     * use the transfer service for full business validation.</p>
     *
     * @param code the code string to validate
     * @return {@code true} if format is valid, {@code false} otherwise
     */
    public boolean isValidFormat(String code) {
        if (code == null || code.length() != codeLength) {
            return false;
        }
        for (char c : code.toCharArray()) {
            if (ALPHABET.indexOf(c) < 0) {
                return false;
            }
        }
        return true;
    }

    // -----------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------

    /**
     * Generates a random code of the configured length — no uniqueness check.
     * Each character is selected uniformly from the 32-character alphabet
     * using {@link SecureRandom}.
     */
    private String generateRaw() {
        StringBuilder sb = new StringBuilder(codeLength);
        int alphabetSize = ALPHABET.length(); // 32 — power of 2, no modulo bias
        for (int i = 0; i < codeLength; i++) {
            sb.append(ALPHABET.charAt(secureRandom.nextInt(alphabetSize)));
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------
    // WithdrawalCode — value object
    // -----------------------------------------------------------------

    /**
     * Immutable value object bundling a withdrawal code with its expiry timestamp.
     *
     * <p>Persist both fields on the {@code Transfer} JPA entity:</p>
     * <pre>{@code
     *   @Column(name = "withdrawal_code", nullable = false, unique = true)
     *   private String withdrawalCode;
     *
     *   @Column(name = "withdrawal_code_expires_at", nullable = false)
     *   private LocalDateTime withdrawalCodeExpiresAt;
     * }</pre>
     */
    public static class WithdrawalCode {

        private final String        code;
        private final LocalDateTime expiresAt;

        public WithdrawalCode(String code, LocalDateTime expiresAt) {
            this.code      = Objects.requireNonNull(code,      "code must not be null");
            this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        }

        /**
         * @return the 8-character withdrawal code string (e.g., {@code "X7KM2PQR"})
         */
        public String getCode() {
            return code;
        }

        /**
         * @return the date and time after which this code is no longer valid
         */
        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }

        /**
         * @return {@code true} if the current time is past the expiry timestamp
         */
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }

        @Override
        public String toString() {
            return "WithdrawalCode{" +
                    "code='" + code + '\'' +
                    ", expiresAt=" + expiresAt +
                    ", expired=" + isExpired() +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WithdrawalCode)) return false;
            WithdrawalCode that = (WithdrawalCode) o;
            return Objects.equals(code, that.code);
        }

        @Override
        public int hashCode() {
            return Objects.hash(code);
        }
    }
}