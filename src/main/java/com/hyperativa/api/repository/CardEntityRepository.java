package com.hyperativa.api.repository;

import com.hyperativa.api.entity.CardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardEntityRepository extends JpaRepository<CardEntity, Long> {
    Page<CardEntity> findByUserId(Long userId, Pageable pageable);

    Optional<CardEntity> findByUserIdAndCardNumberEncrypted(Long userId, String cardNumberEncrypted);

    Optional<CardEntity> findByCardIdentifier(String cardIdentifier);

    boolean existsByCardIdentifier(String cardIdentifier);

    boolean existsByCardNumberEncrypted(String cardNumberEncrypted);
}

