package org.infoufps.application.forum.command;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.forum.ForumThread;
import org.infoufps.domain.forum.IForumLikeRepository;
import org.infoufps.domain.forum.IForumThreadRepository;
import org.infoufps.domain.forum.ThreadId;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.NotFoundException;

/**
 * Caso de uso para dar "Me gusta" a un hilo del foro comunitario.
 * <p>
 * Reglas de negocio:
 * - Requiere cuenta autenticada y activa.
 * - No se puede dar me gusta al hilo propio.
 * - Es una operación idempotente.
 */
@ApplicationScoped
public class LikeForumThreadHandler {

    private final IForumLikeRepository likeRepository;
    private final IForumThreadRepository threadRepository;
    private final IAccountRepository accountRepository;

    @Inject
    public LikeForumThreadHandler(IForumLikeRepository likeRepository,
                                  IForumThreadRepository threadRepository,
                                  IAccountRepository accountRepository) {
        this.likeRepository = likeRepository;
        this.threadRepository = threadRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void execute(String threadIdStr, String accountIdStr) {
        if (threadIdStr == null || threadIdStr.isBlank()) {
            throw new DomainException("El identificador del hilo es obligatorio");
        }
        if (accountIdStr == null || accountIdStr.isBlank()) {
            throw new DomainException("El identificador de la cuenta es obligatorio");
        }

        AccountId accountId = AccountId.of(accountIdStr);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new DomainException("La cuenta especificada no existe: " + accountIdStr));

        if (!account.isActive()) {
            throw new ForbiddenException("La cuenta está suspendida");
        }

        ThreadId threadId = ThreadId.of(threadIdStr);
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new NotFoundException("Hilo no encontrado: " + threadIdStr));

        if (thread.getAuthorId().equals(accountId)) {
            throw new DomainException("No puedes dar me gusta a tu propio hilo");
        }

        likeRepository.addLike(accountId, threadId);
    }
}

