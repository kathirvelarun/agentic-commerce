package com.amex.ace.agent.dto.commerce;
import java.util.UUID;
public record TransactionData(
        UUID transactionReferenceId,
        TransactionAmount transactionAmount,
        String merchantName,
        String merchantCountryCode,
        String merchantUrl,
        UUID mandateId
) {
    /** Convenience constructor that auto-generates a transaction reference ID. */
    public TransactionData(TransactionAmount amount, String merchantName,
                           String merchantCountryCode, String merchantUrl, UUID mandateId) {
        this(UUID.randomUUID(), amount, merchantName, merchantCountryCode, merchantUrl, mandateId);
    }
}
