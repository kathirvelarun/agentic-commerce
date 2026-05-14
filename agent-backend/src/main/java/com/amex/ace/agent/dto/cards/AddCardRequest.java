package com.amex.ace.agent.dto.cards;

import jakarta.validation.constraints.*;

public record AddCardRequest(
        @NotBlank @Size(min = 16, max = 16)         String cardNumber,
        @Min(1)   @Max(12)                           int    expMonth,
        @NotNull                                     Integer expYear,
        @NotBlank @Pattern(regexp = "\\d{3,4}")      String cvv,
        @NotBlank @Size(min = 3, max = 100)          String nameOnCard
) {}
