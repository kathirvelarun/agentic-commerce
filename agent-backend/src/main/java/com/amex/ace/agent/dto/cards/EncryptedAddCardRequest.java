package com.amex.ace.agent.dto.cards;
import jakarta.validation.constraints.NotBlank;
public record EncryptedAddCardRequest(@NotBlank String encPaymentInstrument) {}
