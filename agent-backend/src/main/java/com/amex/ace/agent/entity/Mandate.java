package com.amex.ace.agent.entity;

import com.amex.ace.agent.entity.enums.MandateStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Maps to Python model Mandate (MANDATES table).
 */
@Entity
@Table(name = "MANDATES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mandate {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intent_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_mandate_intent"))
    private Intent intent;

    @Column(length = 20, nullable = false)
    private String amount;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    @Column(name = "effective_until_time", nullable = false)
    private long effectiveUntilTime;

    @Column(length = 255, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MandateStatus status = MandateStatus.ACTIVE;
}
