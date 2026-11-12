package org.infoufps.application.content;

import org.infoufps.domain.content.Content;
import java.util.Set;

public record ModuleItemDto(
        String contentId,
        String title,
        String subtitle,
        String description,
        String imageUrl,
        String categoryTag,
        Set<String> tags,
        String targetUrl,
        int likesCount,
        int commentsCount
) {
    /**
     * Factory method para convertir desde la Entidad de Dominio usando el contador interno.
     */
    public static ModuleItemDto from(Content content) {
        return from(content, content.getStats().getCommentsCount());
    }

    /**
     * Factory method para convertir desde la Entidad de Dominio inyectando el contador calculado de comentarios.
     */
    public static ModuleItemDto from(Content content, int commentsCount) {
        return new ModuleItemDto(
                content.getId().value(),
                content.getTitle(),
                content.getSubtitle(),
                content.getDescription(),
                content.getImageUrl(),
                content.getCategoryTag(),
                content.getTags(),
                content.getTargetUrl(),
                content.getStats().getLikesCount(),
                commentsCount
        );
    }
}
