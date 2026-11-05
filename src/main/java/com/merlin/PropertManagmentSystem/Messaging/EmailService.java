package com.merlin.PropertManagmentSystem.Messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.HtmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient = RestClient.create("https://api.brevo.com/v3");
    private final String apiKey;
    private final String senderName;
    private final String senderEmail;

    public EmailService(
            @Value("${brevo.api-key}") String apiKey,
            @Value("${brevo.sender-name}") String senderName,
            @Value("${brevo.sender-email}") String senderEmail) {
        this.apiKey = apiKey;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
    }

    // The one method that talks to Brevo. Everything else calls this.
    public boolean sendEmail(String toEmail, String toName, String subject, String htmlContent) {

        Map<String, String> recipient = new HashMap<>();
        recipient.put("email", toEmail);
        if (toName != null && !toName.isBlank()) {
            recipient.put("name", toName);
        }

        Map<String, Object> body = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(recipient),
                "subject", subject,
                "htmlContent", htmlContent
        );

        try {
            restClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    // For plain text messages, such as a manual Chairman message or a rent reminder.
    public boolean sendTextEmail(String toEmail, String toName, String subject, String message) {
        String html = "<p>" + HtmlUtils.htmlEscape(message).replace("\n", "<br>") + "</p>";
        return sendEmail(toEmail, toName, subject, html);
    }
}