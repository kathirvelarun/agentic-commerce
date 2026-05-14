package com.amex.ace.agent.dto.cards;
import java.util.UUID;
public record CardResponse(UUID cardId, int expMonth, int expYear,
                           String last4, String status, String tokenId) {}
