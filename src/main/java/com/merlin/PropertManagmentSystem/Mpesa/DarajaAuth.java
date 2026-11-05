package com.merlin.PropertManagmentSystem.Mpesa;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DarajaAuth {

    private final RestTemplate restTemplate;

    @Value("${daraja.base-url}") private String baseUrl;
    @Value("${C2B.consumer-key}") private String consumerKey;
    @Value("${C2B.consumer-secret}") private String consumerSecret;

    private String token;
    private Instant expiresAt = Instant.EPOCH;

    public synchronized String getAccessToken() {
        if (Instant.now().isBefore(expiresAt)) return token;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(consumerKey, consumerSecret);

        JsonNode res = restTemplate.exchange(
                baseUrl + "/oauth/v1/generate?grant_type=client_credentials",
                HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class).getBody();

        token = res.path("access_token").asText();
        expiresAt = Instant.now().plusSeconds(res.path("expires_in").asLong(3599) - 60);
        return token;
    }
}
