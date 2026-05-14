package com.amex.ace.agent.repository;
import com.amex.ace.agent.entity.Intent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface IntentRepository extends JpaRepository<Intent, UUID> {
    Optional<Intent> findByInstructionId(String instructionId);
    List<Intent> findByCardId(UUID cardId);
}
