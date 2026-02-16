package com.hyperativa.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cards", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_card_number_last_digits", columnList = "card_number_last_digits"),
        @Index(name = "idx_card_identifier", columnList = "card_identifier")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "card_number_encrypted", nullable = false, columnDefinition = "TEXT")
    private String cardNumberEncrypted;

    @Column(name = "card_number_last_digits", nullable = false, length = 4)
    private String cardNumberLastDigits;

    @Column(name = "card_identifier", nullable = false)
    private String cardIdentifier;

    @Column(name = "lote_number")
    private String loteNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

