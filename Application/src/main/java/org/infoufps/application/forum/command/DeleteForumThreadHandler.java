package org.infoufps.application.forum.command;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.forum.ForumThread;
import org.infoufps.domain.forum.IForumThreadRepository;
import org.infoufps.domain.forum.ThreadId;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.NotFoundException;

/**
 * Caso de uso para eliminar un hilo (borrado lógico a ARCHIVED).
 * Solo el autor o un administrador tienen permiso.
 */
@ApplicationScoped
public class DeleteForumThreadHandler {

    private final IForumThreadRepository threadRepository;

    @Inject
    public DeleteForumThreadHandler(IForumThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    @Transactional
    public void execute(DeleteForumThreadCommand command) {
        ThreadId threadId = ThreadId.of(command.threadId());
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new NotFoundException("Hilo no encontrado: " + command.threadId()));

        boolean isAuthor = thread.getAuthorId().value().equals(command.actorAccountId());
        if (!isAuthor && !command.isAdmin()) {
            throw new ForbiddenException("No tienes permisos para eliminar este hilo");
        }

        thread.archive();
        threadRepository.save(thread);
    }
}

