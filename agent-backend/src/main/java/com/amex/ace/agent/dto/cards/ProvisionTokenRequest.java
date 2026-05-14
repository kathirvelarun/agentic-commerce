package com.amex.ace.agent.dto.cards;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record ProvisionTokenRequest(@NotNull UUID cardId) {}
