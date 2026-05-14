package com.amex.ace.agent.dto.passkey;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record SolveChallengeRequest(@NotNull UUID clientReferenceId,
                                    @NotNull String provisionedTokenId,
                                    @NotNull String code) {}
