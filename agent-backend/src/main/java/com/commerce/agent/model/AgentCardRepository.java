package com.commerce.agent.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgentCardRepository extends JpaRepository<AgentCard, String> {
    List<AgentCard> findByUserId(String userId);
    Optional<AgentCard> findByUserIdAndIsDefaultTrue(String userId);
    boolean existsByUserIdAndIsDefaultTrue(String userId);
}
