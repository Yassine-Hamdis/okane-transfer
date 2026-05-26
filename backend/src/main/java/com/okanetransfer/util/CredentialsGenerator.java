package com.okanetransfer.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates login credentials for auto-created client accounts.
 *
 * Called by TransferService when:
 *   - Agent creates a transfer
 *   - Sender has no existing OkaneTransfer account
 *   - System auto-creates a ROLE_CLIENT account for them
 *
 * The plain password is sent via NotificationService (email + SMS).
 * It is NEVER stored — only the BCrypt hash is saved to DB.
 * mustChangePassword is set to TRUE so client must change it on first login.
 */
@Component
public class CredentialsGenerator {

    // Password charset: readable characters only
    // Removed: 0, O, l, 1, I  → avoid confusion when reading from SMS
    private static final String UPPERCASE = "ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghjkmnpqrstuvwxyz";
    private static final String DIGITS    = "23456789";
    private static final String SPECIAL   = "@#$!";

    private static final String ALL_CHARS =
            UPPERCASE + LOWERCASE + DIGITS + SPECIAL;

    private static final int PASSWORD_LENGTH = 10;

    private final SecureRandom secureRandom = new SecureRandom();

    // ─────────────────────────────────────────────────────
    //  PASSWORD GENERATION
    // ─────────────────────────────────────────────────────

    /**
     * Generates a secure random password.
     *
     * Rules:
     *  - At least 1 uppercase letter
     *  - At least 1 lowercase letter
     *  - At least 1 digit
     *  - At least 1 special character
     *  - Total length = 10
     *  - No ambiguous characters (0, O, l, 1, I)
     *
     * @return plain text password — pass to NotificationService,
     *         then BCrypt hash before saving to DB
     */
    public String generatePassword() {
        char[] password = new char[PASSWORD_LENGTH];

        // Guarantee at least one of each required type
        password[0] = randomChar(UPPERCASE);
        password[1] = randomChar(LOWERCASE);
        password[2] = randomChar(DIGITS);
        password[3] = randomChar(SPECIAL);

        // Fill the rest randomly from all chars
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password[i] = randomChar(ALL_CHARS);
        }

        // Shuffle to avoid predictable pattern (first 4 chars always typed)
        shuffle(password);

        return new String(password);
    }

    // ─────────────────────────────────────────────────────
    //  EMAIL GENERATION
    // ─────────────────────────────────────────────────────

    /**
     * Generates a placeholder email from the sender's name.
     * Used when the sender has no email address.
     *
     * Format: firstname.lastname.XXXX@okanetransfer.local
     * Example: "john.doe.4872@okanetransfer.local"
     *
     * ".local" domain marks it as auto-generated (not a real email).
     * The client should update it on first login.
     *
     * @param firstName sender first name
     * @param lastName  sender last name
     * @return generated placeholder email
     */
    public String generatePlaceholderEmail(String firstName, String lastName) {
        String cleanFirst = sanitizeName(firstName);
        String cleanLast  = sanitizeName(lastName);
        int    suffix     = 1000 + secureRandom.nextInt(9000); // 4-digit suffix

        return cleanFirst + "." + cleanLast + "." + suffix
                + "@okanetransfer.local";
    }

    /**
     * Generates a full set of credentials in one call.
     *
     * @param firstName sender first name
     * @param lastName  sender last name
     * @param email     real email if provided, null if not
     * @return GeneratedCredentials { email, plainPassword }
     */
    public GeneratedCredentials generate(String firstName,
                                         String lastName,
                                         String email) {
        String resolvedEmail = (email != null && !email.isBlank())
                ? email
                : generatePlaceholderEmail(firstName, lastName);

        String plainPassword = generatePassword();

        return new GeneratedCredentials(resolvedEmail, plainPassword);
    }

    // ─────────────────────────────────────────────────────
    //  INNER CLASS — Result holder
    // ─────────────────────────────────────────────────────

    /**
     * Holds the generated credentials.
     * plainPassword is used ONCE to send via notification,
     * then discarded — never stored anywhere.
     */
    public static class GeneratedCredentials {

        private final String email;
        private final String plainPassword;

        public GeneratedCredentials(String email, String plainPassword) {
            this.email         = email;
            this.plainPassword = plainPassword;
        }

        public String getEmail()         { return email; }
        public String getPlainPassword() { return plainPassword; }

        @Override
        public String toString() {
            // Safety: never log plain password
            return "GeneratedCredentials{email='" + email + "', password='[HIDDEN]'}";
        }
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ─────────────────────────────────────────────────────

    private char randomChar(String charset) {
        return charset.charAt(secureRandom.nextInt(charset.length()));
    }

    /**
     * Fisher-Yates shuffle on char array.
     * Ensures no positional bias in generated password.
     */
    private void shuffle(char[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp  = array[i];
            array[i]   = array[j];
            array[j]   = temp;
        }
    }

    /**
     * Cleans a name for use in email:
     *  - Lowercase
     *  - Remove accents (é→e, à→a, etc.)
     *  - Replace spaces/special chars with empty string
     *  - Max 20 chars
     */
    private String sanitizeName(String name) {
        if (name == null || name.isBlank()) return "user";
        return java.text.Normalizer
                .normalize(name.toLowerCase().trim(),
                        java.text.Normalizer.Form.NFD)
                .replaceAll("[^a-z0-9]", "")
                .substring(0, Math.min(name.length(), 20));
    }
}