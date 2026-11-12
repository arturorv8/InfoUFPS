-- =====================================================================
-- Migración V6: Estado del ciclo de vida de las cuentas
-- =====================================================================

ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_accounts_status ON accounts (status);
