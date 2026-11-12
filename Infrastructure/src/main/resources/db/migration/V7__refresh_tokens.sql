-- =====================================================================
-- Migración V7: Tokens de renovación de sesión
-- =====================================================================
-- Solo se almacena el hash SHA-256 del token; el valor en claro nunca se
-- persiste. Un token revocado conserva su fila para detectar reutilización.

CREATE TABLE IF NOT EXISTS refresh_tokens (
    token_id   VARCHAR(36) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP   NOT NULL,
    expires_at TIMESTAMP   NOT NULL,
    revoked_at TIMESTAMP,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (token_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_refresh_tokens_hash ON refresh_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_account ON refresh_tokens (account_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires ON refresh_tokens (expires_at);
