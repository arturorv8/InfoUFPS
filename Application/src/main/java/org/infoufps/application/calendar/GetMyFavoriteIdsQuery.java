package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.calendar.IFavoriteEventRepository;
import org.infoufps.domain.account.AccountId;

import java.util.List;

/**
 * Consulta liviana de los identificadores de eventos favoritos visibles.
 */
@ApplicationScoped
public class GetMyFavoriteIdsQuery {

    private final IFavoriteEventRepository favoriteRepository;

    @Inject
    public GetMyFavoriteIdsQuery(IFavoriteEventRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * Ejecuta la consulta.
     *
     * @param accountId cuenta autenticada.
     * @return identificadores de eventos publicados favoritos.
     */
    public List<String> execute(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new DomainException("La cuenta es obligatoria");
        }
        return favoriteRepository.findPublishedEventIdsByAccount(AccountId.of(accountId))
                .stream()
                .map(eventId -> eventId.value())
                .toList();
    }
}
