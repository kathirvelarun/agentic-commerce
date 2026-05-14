package com.amex.ace.agent.dto.commerce;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgenticCheckoutResponse(String message,
                                      PurchaseSummary purchaseSummary) {
    public AgenticCheckoutResponse(String message) {
        this(message, null);
    }
}
