package com.amex.ace.agent.repository;
import com.amex.ace.agent.entity.Mandate;
import com.amex.ace.agent.entity.enums.MandateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface MandateRepository extends JpaRepository<Mandate, UUID> {
    List<Mandate> findByIntentId(UUID intentId);
    List<Mandate> findByIntentIdAndStatus(UUID intentId, MandateStatus status);
    List<Mandate> findByIntentIdAndStatusAndEffectiveUntilTimeGreaterThan(
            UUID intentId, MandateStatus status, long threshold);
}
