package com.amex.ace.agent.repository;
import com.amex.ace.agent.entity.CardArtMap;
import com.amex.ace.agent.entity.CardArtMapId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface CardArtMapRepository extends JpaRepository<CardArtMap, CardArtMapId> {
    Optional<CardArtMap> findByPanEnrollmentId(String panEnrollmentId);
    void deleteByPanEnrollmentId(String panEnrollmentId);
}
