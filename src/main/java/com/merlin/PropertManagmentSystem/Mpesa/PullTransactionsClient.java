package com.merlin.PropertManagmentSystem.Mpesa;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PullTransactionsClient {

    private static final DateTimeFormatter PULL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter CALLBACK_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final DarajaAuth darajaAuth;
    private final RestTemplate restTemplate;

    @Value("${c2b.callback-url}") private String baseUrl;

    public List<C2BCallbackPayload> pullTransactions(
            String shortCode, LocalDateTime from, LocalDateTime to, int offset) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(darajaAuth.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "ShortCode", shortCode,
                "StartDate", from.format(PULL_TIME),
                "EndDate", to.format(PULL_TIME),
                "OffSetValue", String.valueOf(offset));

        JsonNode res = restTemplate.postForObject(
                baseUrl + "/pulltransactions/v1/query",
                new HttpEntity<>(body, headers), JsonNode.class);

        String code = res.path("ResponseCode").asText();
        if (!"1000".equals(code)) {
            // do NOT return an empty list here: that would mark the window reconciled by mistake
            throw new IllegalStateException("Pull query failed for " + shortCode + ": " + res);
        }

        List<JsonNode> items = new ArrayList<>();
        collect(res.path("Response"), items);

        List<C2BCallbackPayload> payloads = new ArrayList<>();
        for (JsonNode n : items) {
            payloads.add(toPayload(n, shortCode));
        }
        return payloads;
    }

    private void collect(JsonNode node, List<JsonNode> out) {   // flattens nested arrays
        if (node.isArray()) node.forEach(n -> collect(n, out));
        else if (node.isObject()) out.add(node);
    }

    private C2BCallbackPayload toPayload(JsonNode n, String shortCode) {
        LocalDateTime time = LocalDateTime.parse(n.path("trxDate").asText(), PULL_TIME);

        return new C2BCallbackPayload(
                n.path("transactiontype").asText(null),      // transactionType
                n.path("transactionId").asText(),            // transId
                time.format(CALLBACK_TIME),                  // transTime (yyyyMMddHHmmss)
                new BigDecimal(n.path("amount").asText()),   // transAmount
                shortCode,                                   // businessShortCode, the one you queried
                n.path("billreference").asText(),            // billRefNumber
                n.path("msisdn").asText(null),               // msisdn
                n.path("sender").asText(null),               // firstName (pull gives one name field)
                null,                                        // middleName
                null                                         // lastName
        );
    }

}
