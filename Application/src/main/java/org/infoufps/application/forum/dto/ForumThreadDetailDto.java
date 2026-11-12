package org.infoufps.application.forum.dto;

import java.time.LocalDateTime;

/**
 * DTO para la vista de detalle de un hilo del foro comunitario.
 */
public record ForumThreadDetailDto(
        String id,
        String title,
        String body,
        AuthorSummaryDto author,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean edited,
        long commentsCount,
        long likesCount
) {
}

