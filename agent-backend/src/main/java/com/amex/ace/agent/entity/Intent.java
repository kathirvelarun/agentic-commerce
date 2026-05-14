package com.amex.ace.agent.entity;

import com.amex.ace.agent.entity.enums.IntentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Maps to Python model Intent (INTENTS table).
 */
@Entity
@Table(name = "INTENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Intent {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "instruction_id", length = 50, unique = true, nullable = false)
    private String instructionId;

    /** Nullable FK: ON DELETE SET NULL in Python schema. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @Column(name = "last_4", length = 4, nullable = false)
    private String last4;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
