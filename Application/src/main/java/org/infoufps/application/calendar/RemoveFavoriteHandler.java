package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.calendar.IFavoriteEventRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;

/**
 * Caso de uso para quitar un evento de favoritos.
 * La operación es idempotente.
 */
@ApplicationScoped
public class RemoveFavoriteHandler {

    private final IFavoriteEventRepository favoriteRepository;

    @Inject
    public RemoveFavoriteHandler(IFavoriteEventRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * Quita el favorito si existe.
     *
     * @param command cuenta y evento.
     */
    @Transactional
    public void execute(RemoveFavoriteCommand command) {
        if (command == null || command.accountId() == null || command.accountId().isBlank()) {
            throw new DomainException("La cuenta es obligatoria para quitar un favorito");
        }
        if (command.eventId() == null || command.eventId().isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        favoriteRepository.delete(AccountId.of(command.accountId()), EventId.of(command.eventId()));
    }
}
