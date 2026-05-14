package com.amex.ace.agent.dto.commerce;
import com.fasterxml.jackson.annotation.JsonProperty;
public record TransactionAmount(
        @JsonProperty("transactionAmount")     String transactionAmount,
        @JsonProperty("transactionCurrencyCode") String transactionCurrencyCode
) {}
