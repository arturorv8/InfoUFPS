package org.infoufps.domain.calendar;

/**
 * Enum que representa los estados del ciclo de vida de un contenido y evento de calendario en el sistema.
 * Modela una máquina de estados determinista donde cada estado define explícitamente
 * las transiciones válidas hacia otros estados.
 * <p>
 * NOTA DE ARQUITECTURA: Se evaluó la unificación con {@link org.infoufps.domain.academic.EstadoModeracion}. Aunque son semánticamente
 * análogos (UNDER_REVIEW ≈ EN_REVISION, PUBLISHED ≈ PUBLICADO, ARCHIVED ≈ ARCHIVADO), se mantienen separados
 * para evitar acoplar los subdominios de Contenidos/Calendario (con esquema de persistencia y API en inglés,
 * columna 'status') con el módulo Académico (con esquema de persistencia y API en español, columna 'estado_moderacion').
 * El costo de unificación generaría cambios incompatibles en migraciones Flyway y contratos REST públicos.
 */
public enum Status {

    /**
     * Estado inicial de moderación. El contenido está pendiente de revisión por un administrador.
     * Transiciones permitidas: Aprobado (PUBLISHED) o Rechazado/Archivado directo (ARCHIVED).
     */
    UNDER_REVIEW {
        @Override
        public boolean canTransitionTo(Status nextStatus) {
            return nextStatus == PUBLISHED || nextStatus == ARCHIVED;
        }
    },

    /**
     * Estado público. El contenido fue aprobado y es visible en el feed para los usuarios.
     * Transiciones permitidas: Retirado/Deshabilitado (ARCHIVED) o Re-revisión por cambios críticos/reportes (UNDER_REVIEW).
     */
    PUBLISHED {
        @Override
        public boolean canTransitionTo(Status nextStatus) {
            return nextStatus == ARCHIVED || nextStatus == UNDER_REVIEW;
        }
    },

    /**
     * Estado final o retirado. El contenido fue ocultado o rechazado.
     * Transiciones permitidas: Ninguna (Estado terminal).
     */
    ARCHIVED {
        @Override
        public boolean canTransitionTo(Status nextStatus) {
            return false;
        }
    };

    /**
     * Evalúa si es legal hacer la transición desde el estado actual hacia el estado destino.
     *
     * @param nextStatus El estado objetivo al que se desea pasar.
     * @return true si la transición está permitida por las reglas del ciclo de vida; false de lo contrario.
     */
    public abstract boolean canTransitionTo(Status nextStatus);
}
