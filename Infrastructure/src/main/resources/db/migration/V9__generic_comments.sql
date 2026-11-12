-- =====================================================================
-- Migración V9: Destino genérico y estado para comentarios polimórficos
-- =====================================================================

-- 1. Agregar nuevas columnas
ALTER TABLE comments ADD COLUMN IF NOT EXISTS target_type VARCHAR(50);
ALTER TABLE comments ADD COLUMN IF NOT EXISTS target_id VARCHAR(36);
ALTER TABLE comments ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'PUBLISHED';

-- 2. Copiar comentarios existentes apuntando a su contenido con tipo CONTENT
UPDATE comments
SET target_type = 'CONTENT',
    target_id = content_id,
    status = COALESCE(status, 'PUBLISHED')
WHERE target_type IS NULL;

-- 3. Establecer restricciones NOT NULL en las nuevas columnas
ALTER TABLE comments ALTER COLUMN target_type SET NOT NULL;
ALTER TABLE comments ALTER COLUMN target_id SET NOT NULL;
ALTER TABLE comments ALTER COLUMN status SET NOT NULL;

-- 4. Eliminar índices obsoletos y columna previa content_id
DROP INDEX IF EXISTS idx_comments_content_id;
DROP INDEX IF EXISTS idx_comments_content_author;
DROP INDEX IF EXISTS idx_comments_author_id;
DROP INDEX IF EXISTS idx_comments_created_at;

ALTER TABLE comments DROP COLUMN IF EXISTS content_id;

-- 5. Crear nuevos índices optimizados para destino y antispam
CREATE INDEX IF NOT EXISTS idx_comments_target_status_created
    ON comments (target_type, target_id, status, created_at);

CREATE INDEX IF NOT EXISTS idx_comments_author_created
    ON comments (author_id, created_at);

