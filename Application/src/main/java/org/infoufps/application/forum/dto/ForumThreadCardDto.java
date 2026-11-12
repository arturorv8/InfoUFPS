package org.infoufps.application.forum.dto;

import java.time.LocalDateTime;

/**
 * DTO para la tarjeta resumen de un hilo en el catálogo público y listado personal.
 */
public record ForumThreadCardDto(
        String id,
        String title,
        AuthorSummaryDto author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean edited,
        long commentsCount,
        long likesCount
) {
}

