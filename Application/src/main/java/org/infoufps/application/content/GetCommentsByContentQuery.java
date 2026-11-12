package org.infoufps.application.content;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.content.CommentTargetType;
import org.infoufps.domain.content.ContentId;
import org.infoufps.domain.content.ICommentRepository;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PagedResult;

import java.util.List;

/**
 * Consulta para obtener los comentarios publicados de un contenido.
 */
@ApplicationScoped
public class GetCommentsByContentQuery {

    private final ICommentRepository commentRepository;

    @Inject
    public GetCommentsByContentQuery(ICommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Obtiene todos los comentarios publicados asociados a un contenido específico.
     *
     * @param contentId Identificador del contenido.
     * @return Lista de comentarios ordenados del más antiguo al más reciente.
     */
    public List<CommentDto> execute(String contentId) {
        if (contentId == null || contentId.isBlank()) {
            throw new DomainException("El contentId es obligatorio");
        }
        return commentRepository.findByContentId(ContentId.of(contentId)).stream()
                .map(CommentDto::from)
                .toList();
    }

    /**
     * Obtiene de forma paginada los comentarios publicados asociados a un contenido.
     *
     * @param contentId   Identificador del contenido.
     * @param pageRequest Parámetros de paginación.
     * @return Página de comentarios ordenados del más antiguo al más reciente.
     */
    public PagedResult<CommentDto> execute(String contentId, PageRequest pageRequest) {
        if (contentId == null || contentId.isBlank()) {
            throw new DomainException("El contentId es obligatorio");
        }
        return commentRepository.findPublishedByTarget(CommentTargetType.CONTENT, contentId, pageRequest)
                .map(CommentDto::from);
    }
}
