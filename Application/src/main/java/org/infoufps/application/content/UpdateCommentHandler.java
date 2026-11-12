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
 * Caso de uso para editar un comentario existente.
 * Solo el autor del comentario o un administrador tienen permiso para modificarlo.
 */
@ApplicationScoped
public class UpdateCommentHandler {

    private final ICommentRepository commentRepository;

    @Inject
    public UpdateCommentHandler(ICommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Ejecuta la actualización del texto del comentario.
     *
     * @param command Datos de actualización con identidad y rol del solicitante.
     * @return DTO del comentario modificado.
     */
    @Transactional
    public CommentDto execute(UpdateCommentCommand command) {
        Comment comment = commentRepository.findById(CommentId.of(command.commentId()))
                .orElseThrow(() -> new NotFoundException("Comentario no encontrado: " + command.commentId()));

        boolean isAuthor = comment.getAuthorId().value().equals(command.actorAccountId());
        if (!isAuthor && !command.isAdmin()) {
            throw new ForbiddenException("No tienes permisos para modificar este comentario");
        }

        comment.updateText(command.text());
        commentRepository.save(comment);

        return CommentDto.from(comment);
    }
}

