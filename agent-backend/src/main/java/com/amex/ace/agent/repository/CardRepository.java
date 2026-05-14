package com.amex.ace.agent.repository;

import com.amex.ace.agent.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    Optional<Card> findByTokenId(String tokenId);
    Optional<Card> findByPanEnrollmentId(String panEnrollmentId);
    List<Card> findByStatus(String status);   // used by get_all_active()
}
