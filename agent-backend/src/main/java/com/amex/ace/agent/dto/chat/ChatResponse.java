package com.amex.ace.agent.dto.chat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatResponse(String responseMessage,
                           List<Product> products,
                           OrderSummary orderSummary) {
    public ChatResponse(String responseMessage) {
        this(responseMessage, List.of(), null);
    }
}
