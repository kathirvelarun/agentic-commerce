package com.commerce.agent.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_flow_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentFlowLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String requestId;

    @Column(nullable = false)
    private int stepNumber;

    @Column(nullable = false)
    private String stepLabel;

    @Column(nullable = false)
    private String status; // COMPLETED | ERROR

    @Column(length = 1000)
    private String detail;

    private Long durationMs;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}