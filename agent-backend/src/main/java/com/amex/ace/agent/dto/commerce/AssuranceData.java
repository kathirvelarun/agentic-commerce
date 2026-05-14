package com.amex.ace.agent.dto.commerce;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public record AssuranceData(@NotBlank String identifier,
                            @NotNull FidoAssertionData fidoAssertionData) {}
