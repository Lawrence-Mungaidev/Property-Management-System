package com.merlin.PropertManagmentSystem.Messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@Service
public class SmsService {

    private final RestClient restClient;
    private final String username;
    private final String apiKey;
    private final String senderId; // blank until yours is approved

    public SmsService(
            @Value("${africastalking.base-url}") String baseUrl,
            @Value("${africastalking.username}") String username,
            @Value("${africastalking.api-key}") String apiKey,
            @Value("${africastalking.sender-id:}") String senderId) {
        this.restClient = RestClient.create(baseUrl);
        this.username = username;
        this.apiKey = apiKey;
        this.senderId = senderId;
    }

    public boolean sendSms(String phoneNumber, String message) {
        String to = normalise(phoneNumber);
        if (to == null) {
            log.warn("Invalid phone number, SMS not sent");
            return false;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", username);
        form.add("to", to);
        form.add("message", message);
        if (!senderId.isBlank()) {
            form.add("from", senderId);
        }

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/version1/messaging")
                    .header("apiKey", apiKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
            // TODO: read the per recipient status from the response
            return response != null;
        } catch (RestClientException e) {
            log.error("SMS failed for {}: {}", to, e.getMessage());
            return false;
        }
    }

    // 0712345678 or 712345678 -> +254712345678
    private String normalise(String phone) {
        if (phone == null) return null;
        String p = phone.replaceAll("[\\s-]", "");
        if (p.startsWith("+254")) return p;
        if (p.startsWith("254")) return "+" + p;
        if (p.startsWith("0")) return "+254" + p.substring(1);
        if (p.length() == 9) return "+254" + p;
        return null;
    }
}
