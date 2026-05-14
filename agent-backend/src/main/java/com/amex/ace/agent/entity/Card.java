package com.amex.ace.agent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Maps to Python model Card (CARDS table).
 */
@Entity
@Table(name = "CARDS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "last_4", length = 4, nullable = false)
    private String last4;

    @Column(length = 10)
    private String type;

    @Column(name = "exp_month", nullable = false)
    private int expMonth;

    @Column(name = "exp_year", nullable = false)
    private int expYear;

    @Column(length = 20, nullable = false)
    private String status;   // PENDING | ACTIVE

    @Column(name = "pan_enrollment_id", length = 50, unique = true, nullable = false)
    private String panEnrollmentId;

    @Column(name = "token_id", length = 50, unique = true)
    private String tokenId;
}
