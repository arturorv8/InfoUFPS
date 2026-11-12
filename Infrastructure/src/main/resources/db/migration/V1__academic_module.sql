-- =====================================================================
-- Migración V1: Módulo académico (Profesores, Materias y Reseñas)
-- =====================================================================

-- 1. Tabla de Profesores
CREATE TABLE IF NOT EXISTS profesores (
    profesor_id VARCHAR(36) PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    nombre_normalizado VARCHAR(150) NOT NULL,
    facultad VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE,
    estado VARCHAR(30) NOT NULL,
    proponente_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Índices para Profesores
CREATE INDEX IF NOT EXISTS idx_profesores_estado_facultad ON profesores (estado, facultad);
CREATE INDEX IF NOT EXISTS idx_profesores_nombre_norm ON profesores (nombre_normalizado);
CREATE INDEX IF NOT EXISTS idx_profesores_proponente ON profesores (proponente_id);

-- 2. Tabla de Materias
CREATE TABLE IF NOT EXISTS materias (
    materia_id VARCHAR(36) PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    nombre_normalizado VARCHAR(150) NOT NULL,
    codigo VARCHAR(30) UNIQUE,
    creditos INTEGER,
    tipo VARCHAR(30),
    estado VARCHAR(30) NOT NULL,
    proponente_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Índices para Materias
CREATE INDEX IF NOT EXISTS idx_materias_estado_tipo_creditos ON materias (estado, tipo, creditos);
CREATE INDEX IF NOT EXISTS idx_materias_nombre_norm ON materias (nombre_normalizado);
CREATE INDEX IF NOT EXISTS idx_materias_proponente ON materias (proponente_id);

-- 3. Tabla de Relación Profesor-Materia
CREATE TABLE IF NOT EXISTS profesor_materias (
    profesor_id VARCHAR(36) NOT NULL,
    materia_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (profesor_id, materia_id),
    CONSTRAINT fk_profesor FOREIGN KEY (profesor_id) REFERENCES profesores(profesor_id) ON DELETE CASCADE,
    CONSTRAINT fk_materia FOREIGN KEY (materia_id) REFERENCES materias(materia_id) ON DELETE CASCADE
);

-- 4. Tabla de Reseñas
CREATE TABLE IF NOT EXISTS resenas (
    resena_id VARCHAR(36) PRIMARY KEY,
    autor_id VARCHAR(36) NOT NULL,
    tipo_destino VARCHAR(20) NOT NULL,
    profesor_id VARCHAR(36),
    materia_id VARCHAR(36),
    calificacion INTEGER NOT NULL,
    comentario VARCHAR(1000) NOT NULL,
    materia_cursada VARCHAR(150),
    estado VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_resena_profesor FOREIGN KEY (profesor_id) REFERENCES profesores(profesor_id) ON DELETE RESTRICT,
    CONSTRAINT fk_resena_materia FOREIGN KEY (materia_id) REFERENCES materias(materia_id) ON DELETE RESTRICT
);

-- Un profesor publicado no puede repetir nombre normalizado dentro de la misma facultad.
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_published_profesor_nombre_facultad
    ON profesores (nombre_normalizado, facultad)
    WHERE estado = 'PUBLICADO';

-- Índices para Reseñas
CREATE INDEX IF NOT EXISTS idx_resenas_profesor_estado ON resenas (profesor_id, estado);
CREATE INDEX IF NOT EXISTS idx_resenas_materia_estado ON resenas (materia_id, estado);
CREATE INDEX IF NOT EXISTS idx_resenas_autor_estado ON resenas (autor_id, estado);
CREATE INDEX IF NOT EXISTS idx_resenas_created_at ON resenas (created_at);

-- Unicidad parcial de reseñas no archivadas por usuario y destino
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_profesor_review
    ON resenas (autor_id, profesor_id)
    WHERE estado != 'ARCHIVADO' AND profesor_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_materia_review
    ON resenas (autor_id, materia_id)
    WHERE estado != 'ARCHIVADO' AND materia_id IS NOT NULL;

