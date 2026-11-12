package org.infoufps.application.content;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.content.CommentTargetType;
import org.infoufps.domain.content.ICommentRepository;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PagedResult;

import java.util.List;

/**
 * Consulta genérica para obtener comentarios publicados de cualquier destino polimórfico
 * (contenido, hilo de foro, etc.), ordenados del más antiguo al más reciente.
 */
@ApplicationScoped
public class GetCommentsByTargetQuery {

    private final ICommentRepository commentRepository;

    @Inject
    public GetCommentsByTargetQuery(ICommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Lista completa de comentarios publicados de un destino.
     */
    public List<CommentDto> execute(CommentTargetType targetType, String targetId) {
        if (targetType == null) {
            throw new DomainException("El tipo de destino es obligatorio");
        }
        if (targetId == null || targetId.isBlank()) {
            throw new DomainException("El identificador de destino es obligatorio");
        }
        return commentRepository.findPublishedByTarget(targetType, targetId).stream()
                .map(CommentDto::from)
                .toList();
    }

    /**
     * Lista paginada de comentarios publicados de un destino.
     */
    public PagedResult<CommentDto> execute(CommentTargetType targetType, String targetId, PageRequest pageRequest) {
        if (targetType == null) {
            throw new DomainException("El tipo de destino es obligatorio");
        }
        if (targetId == null || targetId.isBlank()) {
            throw new DomainException("El identificador de destino es obligatorio");
        }
        return commentRepository.findPublishedByTarget(targetType, targetId, pageRequest)
                .map(CommentDto::from);
    }
}

