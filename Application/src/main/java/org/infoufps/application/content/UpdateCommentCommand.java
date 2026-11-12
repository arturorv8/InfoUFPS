package org.infoufps.application.content;

/**
 * Comando para actualizar el texto de un comentario existente.
 */
public record UpdateCommentCommand(
        String commentId,
        String actorAccountId,
        boolean isAdmin,
        String text
) {
}

