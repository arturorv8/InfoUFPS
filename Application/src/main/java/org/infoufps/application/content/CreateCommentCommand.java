package org.infoufps.application.content;

import org.infoufps.domain.content.CommentTargetType;

/**
 * Comando para registrar un comentario en un destino genérico (contenido o hilo).
 */
public record CreateCommentCommand(
        CommentTargetType targetType,
        String targetId,
        String authorId,
        String text
) {
    /**
     * Constructor de compatibilidad para comentarios dirigidos a contenidos.
     */
    public CreateCommentCommand(String contentId, String authorId, String text) {
        this(CommentTargetType.CONTENT, contentId, authorId, text);
    }

    /**
     * Accesor de conveniencia para consumidores previos que requerían contentId.
     */
    public String contentId() {
        return targetId;
    }
}
