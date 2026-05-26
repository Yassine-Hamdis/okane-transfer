package com.okanetransfer.infrastructure.brevo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Sends transactional SMS via Brevo API v3.
 *
 * Brevo SMS API docs:
 * https://developers.brevo.com/reference/sendtransacsms
 *
 * Note: SMS requires credits on your Brevo account.
 * For development: use the test phone number Brevo provides.
 */
@Component
@PropertySource("classpath:application.properties")
public class BrevoSmsSender {

    private static final Logger log =
            LoggerFactory.getLogger(BrevoSmsSender.class);

    private static final String BREVO_SMS_URL =
            "https://api.brevo.com/v3/transactionalSMS/sms";

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sms.sender}")
    private String smsSender;

    /**
     * Sends a transactional SMS via Brevo.
     *
     * @param phoneNumber recipient phone in international format (+212XXXXXXXX)
     * @param message     SMS content (max 160 chars for single SMS)
     */
    public void sendSms(String phoneNumber, String message) {
        try {
            // Sanitize phone: Brevo needs international format
            String cleanPhone = sanitizePhone(phoneNumber);
            if (cleanPhone == null) {
                log.warn("[BREVO SMS] Invalid phone number: {}", phoneNumber);
                return;
            }

            // Truncate message to 160 chars to avoid multi-part SMS billing
            String truncated = message.length() > 160
                    ? message.substring(0, 157) + "..."
                    : message;

            String requestBody = buildSmsPayload(cleanPhone, truncated);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_SMS_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                log.info("[BREVO SMS] Sent to={}", cleanPhone);
            } else {
                log.error("[BREVO SMS] Failed status={} body={}",
                        response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("[BREVO SMS] Exception sending to={}: {}",
                    phoneNumber, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE
    // ─────────────────────────────────────────────────────

    private String buildSmsPayload(String phone, String message) {
        return "{"
                + "\"sender\":\""  + escape(smsSender) + "\","
                + "\"recipient\":\"" + escape(phone)   + "\","
                + "\"content\":\""  + escape(message)  + "\""
                + "}";
    }

    /**
     * Sanitizes phone number to international format.
     * Brevo requires: +[country_code][number]
     * Example: "+212661234567"
     *
     * If already starts with + → keep as-is.
     * If starts with 00 → replace with +.
     * If starts with 0  → assume local, can't auto-fix.
     */
    private String sanitizePhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        String clean = phone.replaceAll("[\\s\\-()]", "");
        if (clean.startsWith("+"))  return clean;
        if (clean.startsWith("00")) return "+" + clean.substring(2);
        // Local number — can't determine country code
        log.warn("[BREVO SMS] Phone {} is not in international format", phone);
        return null;
    }

    private String escape(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}