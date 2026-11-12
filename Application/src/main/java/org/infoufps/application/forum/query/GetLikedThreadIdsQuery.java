package org.infoufps.application.forum.query;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.forum.IForumLikeRepository;

import java.util.Set;

/**
 * Consulta los identificadores de todos los hilos a los que el usuario autenticado ha dado "Me gusta".
 */
@ApplicationScoped
public class GetLikedThreadIdsQuery {

    private final IForumLikeRepository likeRepository;

    @Inject
    public GetLikedThreadIdsQuery(IForumLikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    public Set<String> execute(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return Set.of();
        }
        return likeRepository.findLikedThreadIdsByAccount(AccountId.of(accountId));
    }
}

