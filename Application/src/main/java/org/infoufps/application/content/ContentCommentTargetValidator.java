package org.infoufps.application.content;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.content.CommentTargetType;
import org.infoufps.domain.content.Content;
import org.infoufps.domain.content.ContentId;
import org.infoufps.domain.content.IContentRepository;
import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Validador para destinos de tipo {@link CommentTargetType#CONTENT}.
 * Confirma que el contenido existe y se encuentra en estado {@link Status#PUBLISHED}.
 */
@ApplicationScoped
public class ContentCommentTargetValidator implements ICommentTargetValidator {

    private final IContentRepository contentRepository;

    @Inject
    public ContentCommentTargetValidator(IContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @Override
    public CommentTargetType targetType() {
        return CommentTargetType.CONTENT;
    }

    @Override
    public void validateTargetExistsAndPublished(String targetId) {
        if (targetId == null || targetId.isBlank()) {
            throw new DomainException("El contentId es obligatorio");
        }
        Content content = contentRepository.findById(ContentId.of(targetId))
                .orElseThrow(() -> new DomainException("El contenido especificado no existe: " + targetId));

        if (content.getStatus() != Status.PUBLISHED) {
            throw new DomainException("Solo se pueden comentar contenidos publicados");
        }
    }
}

