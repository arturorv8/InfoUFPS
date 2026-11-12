package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.calendar.PublicEventDto;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.application.calendar.EventDtoAssembler;
import org.infoufps.domain.calendar.IFavoriteEventRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

/**
 * Consulta los eventos favoritos publicados de la cuenta autenticada.
 */
@ApplicationScoped
public class GetMyFavoritesQuery {

    private final IFavoriteEventRepository favoriteRepository;
    private final EventDtoAssembler assembler;

    @Inject
    public GetMyFavoritesQuery(IFavoriteEventRepository favoriteRepository, EventDtoAssembler assembler) {
        this.favoriteRepository = favoriteRepository;
        this.assembler = assembler;
    }

    /**
     * Ejecuta la consulta.
     *
     * @param accountId cuenta autenticada.
     * @param page      página solicitada.
     * @param size      tamaño de página.
     * @return página de eventos favoritos ordenados por fecha del evento.
     */
    public PageResult<PublicEventDto> execute(String accountId, int page, int size) {
        if (accountId == null || accountId.isBlank()) {
            throw new DomainException("La cuenta es obligatoria");
        }
        return assembler.toPublicPagedResult(favoriteRepository.findPublishedFavoriteEvents(
                AccountId.of(accountId), PageRequest.of(page, size)));
    }
}
