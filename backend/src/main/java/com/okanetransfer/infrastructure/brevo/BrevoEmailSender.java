package com.okanetransfer.infrastructure.brevo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Sends transactional emails via Brevo API v3.
 * Uses Java's built-in HttpClient — no extra dependency needed.
 *
 * Brevo free tier: 300 emails/day — enough for development.
 * API docs: https://developers.brevo.com/reference/sendtransacemail
 */
@Component
@PropertySource("classpath:application.properties")
public class BrevoEmailSender {

    private static final Logger log =
            LoggerFactory.getLogger(BrevoEmailSender.class);

    private static final String BREVO_EMAIL_URL =
            "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    /**
     * Sends a transactional email via Brevo.
     *
     * @param toEmail   recipient email address
     * @param toName    recipient display name
     * @param subject   email subject
     * @param textBody  plain text body (always provide this as fallback)
     */
    public void sendEmail(String toEmail,
                          String toName,
                          String subject,
                          String textBody) {
        try {
            String requestBody = buildEmailPayload(
                    toEmail, toName, subject, textBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_EMAIL_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                log.info("[BREVO EMAIL] Sent to={} subject='{}'",
                        toEmail, subject);
            } else {
                log.error("[BREVO EMAIL] Failed status={} body={}",
                        response.statusCode(), response.body());
            }

        } catch (Exception e) {
            // Email failure must NEVER crash the main flow
            log.error("[BREVO EMAIL] Exception sending to={}: {}",
                    toEmail, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE
    // ─────────────────────────────────────────────────────

    private String buildEmailPayload(String toEmail,
                                     String toName,
                                     String subject,
                                     String textBody) {
        return "{"
                + "\"sender\":{"
                +   "\"email\":\"" + senderEmail + "\","
                +   "\"name\":\""  + senderName  + "\""
                + "},"
                + "\"to\":[{"
                +   "\"email\":\"" + escape(toEmail) + "\","
                +   "\"name\":\""  + escape(toName)  + "\""
                + "}],"
                + "\"subject\":\"" + escape(subject)  + "\","
                + "\"textContent\":\"" + escape(textBody) + "\""
                + "}";
    }

    private String escape(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}