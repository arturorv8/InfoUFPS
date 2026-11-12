package org.infoufps.application.forum;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.content.ICommentTargetValidator;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.content.CommentTargetType;
import org.infoufps.domain.forum.ForumThread;
import org.infoufps.domain.forum.IForumThreadRepository;
import org.infoufps.domain.forum.ThreadId;
import org.infoufps.domain.shared.exceptions.DomainException;

/**
 * Validador para destinos de tipo {@link CommentTargetType#FORUM_THREAD}.
 * Confirma que el hilo exista y se encuentre en estado {@link Status#PUBLISHED}.
 * Aplica la regla: "Un hilo no publicado no admite comentarios nuevos".
 */
@ApplicationScoped
public class ForumThreadCommentTargetValidator implements ICommentTargetValidator {

    private final IForumThreadRepository threadRepository;

    @Inject
    public ForumThreadCommentTargetValidator(IForumThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    @Override
    public CommentTargetType targetType() {
        return CommentTargetType.FORUM_THREAD;
    }

    @Override
    public void validateTargetExistsAndPublished(String targetId) {
        if (targetId == null || targetId.isBlank()) {
            throw new DomainException("El threadId es obligatorio");
        }
        ForumThread thread = threadRepository.findById(ThreadId.of(targetId))
                .orElseThrow(() -> new DomainException("El hilo especificado no existe: " + targetId));

        if (thread.getStatus() != Status.PUBLISHED) {
            throw new DomainException("Un hilo no publicado no admite comentarios nuevos");
        }
    }
}

