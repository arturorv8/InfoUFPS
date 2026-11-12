package org.infoufps.application.forum.command;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.forum.IForumLikeRepository;
import org.infoufps.domain.forum.ThreadId;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;

/**
 * Caso de uso para quitar "Me gusta" de un hilo del foro comunitario.
 * Operación idempotente.
 */
@ApplicationScoped
public class UnlikeForumThreadHandler {

    private final IForumLikeRepository likeRepository;
    private final IAccountRepository accountRepository;

    @Inject
    public UnlikeForumThreadHandler(IForumLikeRepository likeRepository,
                                    IAccountRepository accountRepository) {
        this.likeRepository = likeRepository;
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
        likeRepository.removeLike(accountId, threadId);
    }
}

