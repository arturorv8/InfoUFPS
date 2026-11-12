package org.infoufps.application.content;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.content.Comment;
import org.infoufps.domain.content.CommentId;
import org.infoufps.domain.content.ICommentRepository;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.NotFoundException;

/**
 * Caso de uso para eliminar un comentario.
 * Aplica borrado lógico pasando el estado a {@code ARCHIVED}.
 * Solo el autor del comentario o un administrador tienen permiso.
 */
@ApplicationScoped
public class DeleteCommentHandler {

    private final ICommentRepository commentRepository;

    @Inject
    public DeleteCommentHandler(ICommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Ejecuta el borrado lógico del comentario.
     *
     * @param command Datos con el identificador del comentario e identidad del solicitante.
     */
    @Transactional
    public void execute(DeleteCommentCommand command) {
        Comment comment = commentRepository.findById(CommentId.of(command.commentId()))
                .orElseThrow(() -> new NotFoundException("Comentario no encontrado: " + command.commentId()));

        boolean isAuthor = comment.getAuthorId().value().equals(command.actorAccountId());
        if (!isAuthor && !command.isAdmin()) {
            throw new ForbiddenException("No tienes permisos para eliminar este comentario");
        }

        comment.archive();
        commentRepository.save(comment);
    }
}

