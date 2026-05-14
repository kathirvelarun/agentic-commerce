package com.amex.ace.agent.entity;

import com.amex.ace.agent.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Maps to Python model Transaction (TRANSACTIONS table).
 */
@Entity
@Table(name = "TRANSACTIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mandate_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_transaction_mandate"))
    private Mandate mandate;

    @Column(length = 20, nullable = false)
    private String amount;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    @Column(name = "merchant_name", length = 255)
    private String merchantName;

    @Column(name = "merchant_country_code", length = 2)
    private String merchantCountryCode;

    @Column(name = "merchant_url", length = 2048)
    private String merchantUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.ACTIVE;
}
