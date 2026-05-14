package com.amex.ace.agent.dto.chat;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
public record ChatRequest(@NotBlank String message, List<ProductInput> products) {
    public List<ProductInput> products() {
        return products != null ? products : List.of();
    }
}
