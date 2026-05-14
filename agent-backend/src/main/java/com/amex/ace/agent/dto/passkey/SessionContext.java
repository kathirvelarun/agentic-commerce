package com.amex.ace.agent.dto.passkey;
import jakarta.validation.constraints.NotBlank;
public record SessionContext(@NotBlank String secureToken) {}
