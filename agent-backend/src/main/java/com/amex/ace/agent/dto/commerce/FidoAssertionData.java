package com.amex.ace.agent.dto.commerce;
import jakarta.validation.constraints.NotBlank;
public record FidoAssertionData(@NotBlank String code) {}
