package org.infoufps.application.content;

import org.infoufps.domain.content.Comment;
import org.infoufps.domain.content.CommentTargetType;
import org.infoufps.domain.calendar.Status;

import java.time.LocalDateTime;

/**
 * DTO que representa un comentario genérico en la API pública.
 */
public record CommentDto(
        String commentId,
        String targetType,
        String targetId,
        String contentId,
        String authorId,
        String text,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Constructor de conveniencia para mantener compatibilidad hacia atrás con firmas previas de 6 argumentos.
     */
    public CommentDto(String commentId, String contentId, String authorId, String text,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(
                commentId,
                CommentTargetType.CONTENT.name(),
                contentId,
                contentId,
                authorId,
                text,
                Status.PUBLISHED.name(),
                createdAt,
                updatedAt
        );
    }

    /**
     * Mapea una entidad de dominio {@link Comment} a su DTO correspondiente.
     *
     * @param comment Entidad de dominio.
     * @return Instancia inmutable de {@link CommentDto}.
     */
    public static CommentDto from(Comment comment) {
        String cid = comment.getTargetType() == CommentTargetType.CONTENT ? comment.getTargetId() : null;
        return new CommentDto(
                comment.getId().value(),
                comment.getTargetType() != null ? comment.getTargetType().name() : null,
                comment.getTargetId(),
                cid,
                comment.getAuthorId().value(),
                comment.getText(),
                comment.getStatus() != null ? comment.getStatus().name() : null,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
