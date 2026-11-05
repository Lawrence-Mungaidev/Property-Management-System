package com.merlin.PropertManagmentSystem.Mpesa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record C2BCallbackPayload(
        @JsonProperty("TransactionType")   String transactionType,
        @JsonProperty("TransID")           String transId,
        @JsonProperty("TransTime")         String transTime,          // yyyyMMddHHmmss, Nairobi time
        @JsonProperty("TransAmount")       BigDecimal transAmount,
        @JsonProperty("BusinessShortCode") String businessShortCode,
        @JsonProperty("BillRefNumber")     String billRefNumber,      // the account number the tenant typed
        @JsonProperty("MSISDN")            String msisdn,             // masked, display only
        @JsonProperty("FirstName")         String firstName,
        @JsonProperty("MiddleName")        String middleName,
        @JsonProperty("LastName")          String lastName
) {}
