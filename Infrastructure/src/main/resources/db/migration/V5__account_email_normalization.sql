-- =====================================================================
-- Migración V5: Normalización de emails y unicidad sin distinguir mayúsculas
-- =====================================================================
-- El dominio normaliza el email (strip + minúsculas) antes de persistirlo.
-- Esta migración alinea los datos existentes y refuerza la regla en la base
-- de datos con un índice único funcional, de modo que dos registros
-- concurrentes no puedan crear variantes del mismo email.

UPDATE accounts
SET email = LOWER(TRIM(email))
WHERE email <> LOWER(TRIM(email));

CREATE UNIQUE INDEX IF NOT EXISTS idx_accounts_email_lower
    ON accounts (LOWER(email));
