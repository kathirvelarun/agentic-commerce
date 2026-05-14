package com.amex.ace.agent.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Maps to Python model CardArt (CARD_ART table).
 * Stores card art images fetched from VTS.
 */
@Entity
@Table(name = "CARD_ART")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardArt {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "mime_type", length = 255, nullable = false)
    private String mimeType;

    @Lob
    @Column(name = "data", nullable = false)
    private byte[] data;
}
