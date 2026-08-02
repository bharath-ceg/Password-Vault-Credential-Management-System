-- ============================================================
-- SecureVault Database Migration
-- Add is_used column to email_verification_tokens table
-- Run this script against your PostgreSQL database if the
-- backend throws an SQL exception about missing 'is_used' column.
-- ============================================================

-- Step 1: Add the is_used column if it does not already exist
ALTER TABLE email_verification_tokens
    ADD COLUMN IF NOT EXISTS is_used boolean NOT NULL DEFAULT false;

-- Step 2: Update any existing rows (should already be false by default)
UPDATE email_verification_tokens
SET is_used = false
WHERE is_used IS NULL;

-- Step 3: Verify the column was added
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'email_verification_tokens'
AND column_name = 'is_used';
