CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `users_email_unique_key` (`email`),
  UNIQUE KEY `users_username_unique_key` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `card_batches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `lote_number` varchar(255) NOT NULL,
  `processed_cards` int DEFAULT NULL,
  `status` enum('COMPLETED','FAILED','PENDING','PROCESSING') NOT NULL,
  `total_cards` int NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKge816m7vhxm1xwickc9dyk5db` (`lote_number`),
  KEY `idx_lote_number` (`lote_number`),
  KEY `idx_batch_user_id` (`user_id`),
  CONSTRAINT `card_batches_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cards` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `card_identifier` varchar(255) NOT NULL,
  `card_number_encrypted` text NOT NULL,
  `card_number_last_digits` varchar(4) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `lote_number` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_card_number_last_digits` (`card_number_last_digits`),
  KEY `idx_card_identifier` (`card_identifier`),
  CONSTRAINT `cards_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
