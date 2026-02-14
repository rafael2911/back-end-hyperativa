package com.hyperativa.api.repository;

import com.hyperativa.api.entity.CardBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardBatchEntityRepository extends JpaRepository<CardBatchEntity, Long> {
    Optional<CardBatchEntity> findByLoteNumber(String loteNumber);
    boolean existsByLoteNumber(String loteNumber);
}

