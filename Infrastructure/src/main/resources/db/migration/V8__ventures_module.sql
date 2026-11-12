-- =====================================================================
-- Migración V8: Módulo de Emprendimientos (La Vitrina)
-- =====================================================================

-- 0. Email verificado en accounts (mínimo requerido para calificar)
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT TRUE;

-- 1. Tabla principal de emprendimientos
CREATE TABLE IF NOT EXISTS ventures (
    venture_id     VARCHAR(36)   NOT NULL,
    name           VARCHAR(100)  NOT NULL,
    description    VARCHAR(1000) NOT NULL,
    category       VARCHAR(50)   NOT NULL,
    owner_id       VARCHAR(36)   NOT NULL,
    contact_type   VARCHAR(20)   NOT NULL,
    contact_value  VARCHAR(512)  NOT NULL,
    location       VARCHAR(255),
    active         BOOLEAN       NOT NULL DEFAULT TRUE,
    status         VARCHAR(30)   NOT NULL DEFAULT 'UNDER_REVIEW',
    created_at     TIMESTAMP     NOT NULL,
    updated_at     TIMESTAMP     NOT NULL,
    published_at   TIMESTAMP,
    name_search    VARCHAR(255)  NOT NULL,
    version        BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ventures PRIMARY KEY (venture_id)
);

-- Índices requeridos para búsqueda y moderación
CREATE INDEX IF NOT EXISTS idx_ventures_status_active_category ON ventures (status, active, category);
CREATE INDEX IF NOT EXISTS idx_ventures_name_search ON ventures (name_search);
CREATE INDEX IF NOT EXISTS idx_ventures_owner_id ON ventures (owner_id);
CREATE INDEX IF NOT EXISTS idx_ventures_contact_value ON ventures (contact_value);

-- 2. Modos de servicio asociados al emprendimiento
CREATE TABLE IF NOT EXISTS venture_service_modes (
    venture_id   VARCHAR(36) NOT NULL,
    service_mode VARCHAR(30) NOT NULL,
    CONSTRAINT pk_venture_service_modes PRIMARY KEY (venture_id, service_mode),
    CONSTRAINT fk_venture_service_modes_venture FOREIGN KEY (venture_id)
        REFERENCES ventures (venture_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_venture_service_modes_mode ON venture_service_modes (service_mode);

-- 3. Calificaciones de emprendimientos (estrellas 1 a 5, única por pareja cuenta-emprendimiento)
CREATE TABLE IF NOT EXISTS venture_ratings (
    account_id VARCHAR(36) NOT NULL,
    venture_id VARCHAR(36) NOT NULL,
    score      INT         NOT NULL CHECK (score >= 1 AND score <= 5),
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP   NOT NULL,
    CONSTRAINT pk_venture_ratings PRIMARY KEY (account_id, venture_id),
    CONSTRAINT fk_venture_ratings_venture FOREIGN KEY (venture_id)
        REFERENCES ventures (venture_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_venture_ratings_venture ON venture_ratings (venture_id);

-- 4. Emprendimientos favoritos (único por pareja cuenta-emprendimiento)
CREATE TABLE IF NOT EXISTS favorite_ventures (
    account_id VARCHAR(36) NOT NULL,
    venture_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP   NOT NULL,
    CONSTRAINT pk_favorite_ventures PRIMARY KEY (account_id, venture_id),
    CONSTRAINT fk_favorite_ventures_venture FOREIGN KEY (venture_id)
        REFERENCES ventures (venture_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_favorite_ventures_account ON favorite_ventures (account_id);

-- 5. Reportes de moderación (único por pareja reportante-emprendimiento)
CREATE TABLE IF NOT EXISTS venture_reports (
    report_id   VARCHAR(36)  NOT NULL,
    reporter_id VARCHAR(36)  NOT NULL,
    venture_id  VARCHAR(36)  NOT NULL,
    reason      VARCHAR(50)  NOT NULL,
    comment     VARCHAR(500),
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP    NOT NULL,
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(36),
    CONSTRAINT pk_venture_reports PRIMARY KEY (report_id),
    CONSTRAINT uq_venture_reports_reporter_venture UNIQUE (reporter_id, venture_id),
    CONSTRAINT fk_venture_reports_venture FOREIGN KEY (venture_id)
        REFERENCES ventures (venture_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_venture_reports_venture_status ON venture_reports (venture_id, status);
CREATE INDEX IF NOT EXISTS idx_venture_reports_status ON venture_reports (status);

