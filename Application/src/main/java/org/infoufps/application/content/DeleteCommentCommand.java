package org.infoufps.application.content;

/**
 * Comando para eliminar (borrado lógico a ARCHIVED) un comentario.
 */
public record DeleteCommentCommand(
        String commentId,
        String actorAccountId,
        boolean isAdmin
) {
}

