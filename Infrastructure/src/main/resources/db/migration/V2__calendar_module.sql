-- =====================================================================
-- Migración V2: Módulo Calendario InfoUFPS
-- Zona horaria de negocio: America/Bogota (fechas y horas locales).
-- =====================================================================

CREATE TABLE IF NOT EXISTS events (
    event_id         VARCHAR(36)  NOT NULL,
    title            VARCHAR(255) NOT NULL,
    event_type       VARCHAR(50)  NOT NULL,
    location         VARCHAR(255) NOT NULL,
    start_date       DATE         NOT NULL,
    end_date         DATE         NOT NULL,
    start_time       TIME,
    end_time         TIME,
    description      VARCHAR(2000),
    info_url         VARCHAR(512),
    status           VARCHAR(30)  NOT NULL,
    proposed_by      VARCHAR(36)  NOT NULL,
    created_by_admin BOOLEAN      NOT NULL DEFAULT FALSE,
    published_at     TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    revision         INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT pk_events PRIMARY KEY (event_id)
);

CREATE INDEX IF NOT EXISTS idx_events_status_type_start ON events (status, event_type, start_date);
CREATE INDEX IF NOT EXISTS idx_events_status_end ON events (status, end_date);
CREATE INDEX IF NOT EXISTS idx_events_proposed_by ON events (proposed_by);

CREATE TABLE IF NOT EXISTS favorite_events (
    account_id VARCHAR(36) NOT NULL,
    event_id   VARCHAR(36) NOT NULL,
    created_at TIMESTAMP   NOT NULL,
    CONSTRAINT pk_favorite_events PRIMARY KEY (account_id, event_id),
    CONSTRAINT fk_favorite_events_event FOREIGN KEY (event_id)
        REFERENCES events (event_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_favorite_events_event_id ON favorite_events (event_id);
