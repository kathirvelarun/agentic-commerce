package com.amex.ace.agent.entity;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardArtMapId implements Serializable {
    private String panEnrollmentId;
    private String cardArtId;
}
