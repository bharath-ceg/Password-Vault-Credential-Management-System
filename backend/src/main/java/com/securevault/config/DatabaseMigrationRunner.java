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

        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS credential_shares (" +
                "    id BIGSERIAL PRIMARY KEY," +
                "    credential_id BIGINT NOT NULL REFERENCES vault_credentials(id) ON DELETE CASCADE," +
                "    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE," +
                "    recipient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE," +
                "    permission_level VARCHAR(30) NOT NULL DEFAULT 'VIEW_ONLY'," +
                "    created_at TIMESTAMP WITH TIME ZONE NOT NULL," +
                "    expires_at TIMESTAMP WITH TIME ZONE," +
                "    updated_at TIMESTAMP WITH TIME ZONE NOT NULL," +
                "    CONSTRAINT uq_credential_recipient UNIQUE (credential_id, recipient_id)" +
                ");"
            );

            jdbcTemplate.execute(
                "ALTER TABLE credential_shares ADD COLUMN IF NOT EXISTS permission_level VARCHAR(30) NOT NULL DEFAULT 'VIEW_ONLY';"
            );

            jdbcTemplate.execute("ALTER TABLE credential_shares DROP CONSTRAINT IF EXISTS credential_shares_permission_level_check;");
            jdbcTemplate.execute("ALTER TABLE credential_shares DROP CONSTRAINT IF EXISTS credential_shares_permission_check;");
            jdbcTemplate.execute("ALTER TABLE credential_shares DROP CONSTRAINT IF EXISTS chk_credential_shares_permission_level;");

            jdbcTemplate.execute(
                "ALTER TABLE credential_shares ADD CONSTRAINT chk_credential_shares_permission_level " +
                "CHECK (permission_level IN ('VIEW_ONLY', 'EDIT_ACCESS', 'FULL_MANAGEMENT'));"
            );

            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_shares_owner ON credential_shares(owner_id);");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_shares_recipient ON credential_shares(recipient_id);");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_shares_credential ON credential_shares(credential_id);");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_shares_expires ON credential_shares(expires_at);");

            // Ensure foreign key constraint on credential_id has ON DELETE CASCADE
            try {
                jdbcTemplate.execute("ALTER TABLE credential_shares DROP CONSTRAINT IF EXISTS fkfx0hqf2usrfkt56njca7737e5;");
                jdbcTemplate.execute("ALTER TABLE credential_shares DROP CONSTRAINT IF EXISTS fk_shares_credential;");
                jdbcTemplate.execute(
                    "ALTER TABLE credential_shares ADD CONSTRAINT fk_shares_credential " +
                    "FOREIGN KEY (credential_id) REFERENCES vault_credentials(id) ON DELETE CASCADE;"
                );
            } catch (Exception fkEx) {
                log.warn("FK constraint adjustment notice: {}", fkEx.getMessage());
            }

            log.info("Migration applied: credential_shares permission_level schema and constraints ensured.");
        } catch (Exception e) {
            log.warn("Migration warning for credential_shares: {}", e.getMessage());
        }

        log.info("Database schema migrations completed.");
    }
}
