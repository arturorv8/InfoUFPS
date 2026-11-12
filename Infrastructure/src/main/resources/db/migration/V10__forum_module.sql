-- =====================================================================
-- Migración V10: Módulo de Foro Comunitario
-- =====================================================================

-- 1. Tabla principal de hilos del foro
CREATE TABLE IF NOT EXISTS forum_threads (
    thread_id  VARCHAR(36)  NOT NULL,
    title      VARCHAR(150) NOT NULL,
    body       TEXT         NOT NULL,
    author_id  VARCHAR(36)  NOT NULL,
    status     VARCHAR(30)  NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP,
    version    BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_forum_threads PRIMARY KEY (thread_id)
);

CREATE INDEX IF NOT EXISTS idx_forum_threads_status_created ON forum_threads (status, created_at);
CREATE INDEX IF NOT EXISTS idx_forum_threads_author_created ON forum_threads (author_id, created_at);

-- 2. Interacciones "Me gusta" sobre hilos del foro
CREATE TABLE IF NOT EXISTS forum_likes (
    account_id VARCHAR(36) NOT NULL,
    thread_id  VARCHAR(36) NOT NULL,
    created_at TIMESTAMP   NOT NULL,
    version    BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_forum_likes PRIMARY KEY (account_id, thread_id)
);

CREATE INDEX IF NOT EXISTS idx_forum_likes_thread_id ON forum_likes (thread_id);
CREATE INDEX IF NOT EXISTS idx_forum_likes_account_id ON forum_likes (account_id);

-- 3. Reportes de moderación sobre hilos o comentarios del foro
CREATE TABLE IF NOT EXISTS forum_reports (
    report_id   VARCHAR(36)  NOT NULL,
    reporter_id VARCHAR(36)  NOT NULL,
    target_type VARCHAR(20)  NOT NULL,
    target_id   VARCHAR(36)  NOT NULL,
    reason      VARCHAR(50)  NOT NULL,
    comment     VARCHAR(500),
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP    NOT NULL,
    resolved_at TIMESTAMP,
    version     BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_forum_reports PRIMARY KEY (report_id),
    CONSTRAINT uq_forum_reports_reporter_target UNIQUE (reporter_id, target_type, target_id)
);

CREATE INDEX IF NOT EXISTS idx_forum_reports_target_status ON forum_reports (target_type, target_id, status);
CREATE INDEX IF NOT EXISTS idx_forum_reports_status ON forum_reports (status);

