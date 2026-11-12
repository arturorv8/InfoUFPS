package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.venture.IFavoriteVentureRepository;
import org.infoufps.domain.venture.VentureId;

import java.util.List;

/**
 * Consulta de los identificadores de emprendimientos marcados como favoritos por la cuenta autenticada.
 */
@ApplicationScoped
public class GetMyFavoriteIdsQuery {

    private final IFavoriteVentureRepository favoriteRepository;

    @Inject
    public GetMyFavoriteIdsQuery(IFavoriteVentureRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public List<String> execute(String accountId) {
        return favoriteRepository.findFavoriteVentureIdsByAccountId(AccountId.of(accountId))
                .stream()
                .map(VentureId::value)
                .toList();
    }
}

