package com.securevault.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * DatabaseMigrationRunner executes idempotent schema migrations on every
 * application startup, before Hibernate's ddl-auto and before DataInitializer.
 *
 * This runner is required because Hibernate's ddl-auto=update cannot safely
 * ALTER an existing table to add a NOT NULL column with a DEFAULT value on
 * PostgreSQL when rows already exist. Running an explicit "ADD COLUMN IF NOT
 * EXISTS" is the correct and idempotent way to handle this scenario.
 */
@Component
@Order(1) // Runs before DataInitializer (which is @Order(2) or unordered)
public class DatabaseMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Running database schema migrations...");

        // Migration: Add is_used column to email_verification_tokens if missing.
        // This column was introduced to track whether a verification token has
        // already been consumed, preventing token reuse attacks.
        try {
            jdbcTemplate.execute(
                "ALTER TABLE email_verification_tokens " +
                "ADD COLUMN IF NOT EXISTS is_used boolean NOT NULL DEFAULT false"
            );
            log.info("Migration applied: email_verification_tokens.is_used ensured.");
        } catch (Exception e) {
            log.warn("Migration warning for email_verification_tokens.is_used: {}", e.getMessage());
        }

        log.info("Database schema migrations completed.");
    }
}
