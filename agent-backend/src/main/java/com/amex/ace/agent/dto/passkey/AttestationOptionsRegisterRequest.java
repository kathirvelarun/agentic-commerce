package com.amex.ace.agent.dto.passkey;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record AttestationOptionsRegisterRequest(
        @NotNull UUID clientReferenceId,
        @NotNull SessionContext sessionContext,
        @NotNull BrowserData browserData,
        @NotNull String provisionedTokenId
) {}
