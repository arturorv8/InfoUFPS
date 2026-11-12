package org.infoufps.domain.content;

import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Define los tipos de destino polimórficos a los cuales se puede asociar un comentario.
 */
public enum CommentTargetType {
    CONTENT,
    FORUM_THREAD;

    /**
     * Parsea de manera segura un valor de texto al enum correspondiente.
     *
     * @param value Nombre del tipo de destino.
     * @return El {@link CommentTargetType} correspondiente.
     * @throws DomainException Si el valor no corresponde a un tipo válido.
     */
    public static CommentTargetType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El tipo de destino es obligatorio");
        }
        try {
            return CommentTargetType.valueOf(value.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Tipo de destino inválido: " + value + ". Valores permitidos: CONTENT, FORUM_THREAD");
        }
    }
}

