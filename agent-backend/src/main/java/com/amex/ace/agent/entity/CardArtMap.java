package com.amex.ace.agent.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Maps to Python model CardArtMap (CARD_ART_MAP table).
 * Links a PAN enrollment ID to a card art asset.
 */
@Entity
@Table(name = "CARD_ART_MAP")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CardArtMapId.class)
public class CardArtMap {

    @Id
    @Column(name = "pan_enrollment_id", length = 50)
    private String panEnrollmentId;

    @Id
    @Column(name = "card_art_id", length = 50)
    private String cardArtId;
}
