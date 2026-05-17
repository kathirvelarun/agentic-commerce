package com.commerce.agent.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AgentFlowLogRepository extends JpaRepository<AgentFlowLog, String> {
    List<AgentFlowLog> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    List<AgentFlowLog> findTop200ByOrderByCreatedAtDesc();

    @Transactional
    void deleteBySessionId(String sessionId);
}