package com.amex.ace.agent.repository;
import com.amex.ace.agent.entity.CardArt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface CardArtRepository extends JpaRepository<CardArt, String> {}
