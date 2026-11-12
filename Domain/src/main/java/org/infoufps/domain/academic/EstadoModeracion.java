package org.infoufps.domain.academic;

import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Estado del ciclo de vida y moderación para las entidades del módulo académico (Profesor, Materia, Reseña).
 * <p>
 * NOTA DE ARQUITECTURA: Se evaluó la unificación con {@link Status}. Aunque comparten transiciones equivalentes,
 * se preservan como enums independientes para delimitar los contextos acotados (Bounded Contexts) y evitar
 * romper la persistencia relacional existente (columna 'estado_moderacion') ni los contratos de API en español.
 */
public enum EstadoModeracion {
    EN_REVISION {
        @Override
        public boolean canTransitionTo(EstadoModeracion next) {
            return next == PUBLICADO || next == ARCHIVADO;
        }
    },
    PUBLICADO {
        @Override
        public boolean canTransitionTo(EstadoModeracion next) {
            return next == ARCHIVADO;
        }
    },
    ARCHIVADO {
        @Override
        public boolean canTransitionTo(EstadoModeracion next) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(EstadoModeracion next);

    public static EstadoModeracion fromString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (EstadoModeracion e : values()) {
            if (e.name().equalsIgnoreCase(text.trim())) {
                return e;
            }
        }
        throw new DomainException("Estado desconocido: " + text);
    }
}

