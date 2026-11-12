package org.infoufps.application.forum.command;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.forum.dto.AuthorSummaryDto;
import org.infoufps.application.forum.dto.ForumThreadDetailDto;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.content.CommentTargetType;
import org.infoufps.domain.content.ICommentRepository;
import org.infoufps.domain.forum.ForumThread;
import org.infoufps.domain.forum.IForumLikeRepository;
import org.infoufps.domain.forum.IForumThreadRepository;
import org.infoufps.domain.forum.ThreadId;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.NotFoundException;

/**
 * Caso de uso para editar un hilo existente. Solo el autor o un administrador tienen permiso.
 */
@ApplicationScoped
public class UpdateForumThreadHandler {

    private final IForumThreadRepository threadRepository;
    private final IAccountRepository accountRepository;
    private final ICommentRepository commentRepository;
    private final IForumLikeRepository likeRepository;

    @Inject
    public UpdateForumThreadHandler(IForumThreadRepository threadRepository,
                                    IAccountRepository accountRepository,
                                    ICommentRepository commentRepository,
                                    IForumLikeRepository likeRepository) {
        this.threadRepository = threadRepository;
        this.accountRepository = accountRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    @Transactional
    public ForumThreadDetailDto execute(UpdateForumThreadCommand command) {
        ThreadId threadId = ThreadId.of(command.threadId());
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new NotFoundException("Hilo no encontrado: " + command.threadId()));

        boolean isAuthor = thread.getAuthorId().value().equals(command.actorAccountId());
        if (!isAuthor && !command.isAdmin()) {
            throw new ForbiddenException("No tienes permisos para editar este hilo");
        }

        thread.update(command.title(), command.body());
        threadRepository.save(thread);

        Account author = accountRepository.findById(thread.getAuthorId()).orElse(null);
        AuthorSummaryDto authorDto = AuthorSummaryDto.from(thread.getAuthorId(), author);

        long commentsCount = commentRepository.countPublishedByTarget(CommentTargetType.FORUM_THREAD, thread.getId().value());
        long likesCount = likeRepository.countByThreadId(thread.getId());

        return new ForumThreadDetailDto(
                thread.getId().value(),
                thread.getTitle(),
                thread.getBody(),
                authorDto,
                thread.getStatus().name(),
                thread.getCreatedAt(),
                thread.getUpdatedAt(),
                thread.isEdited(),
                commentsCount,
                likesCount
        );
    }
}

