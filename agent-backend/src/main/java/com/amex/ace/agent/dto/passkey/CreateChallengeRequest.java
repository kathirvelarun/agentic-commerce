package com.amex.ace.agent.dto.passkey;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record CreateChallengeRequest(@NotNull UUID clientReferenceId,
                                     @NotNull String provisionedTokenId,
                                     @NotNull String identifier) {}
