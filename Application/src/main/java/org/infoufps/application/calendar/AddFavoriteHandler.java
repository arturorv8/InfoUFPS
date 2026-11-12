package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.FavoriteEvent;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.application.calendar.PublishedEventLoader;
import org.infoufps.domain.calendar.IFavoriteEventRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;

/**
 * Caso de uso para marcar un evento como favorito.
 * La operación es idempotente y solo admite eventos publicados.
 */
@ApplicationScoped
public class AddFavoriteHandler {

    private final IFavoriteEventRepository favoriteRepository;
    private final PublishedEventLoader eventLoader;

    @Inject
    public AddFavoriteHandler(IFavoriteEventRepository favoriteRepository, PublishedEventLoader eventLoader) {
        this.favoriteRepository = favoriteRepository;
        this.eventLoader = eventLoader;
    }

    /**
     * Marca el evento como favorito.
     *
     * @param command cuenta y evento.
     */
    @Transactional
    public void execute(AddFavoriteCommand command) {
        if (command == null || command.accountId() == null || command.accountId().isBlank()) {
            throw new DomainException("La cuenta es obligatoria para marcar un favorito");
        }
        if (command.eventId() == null || command.eventId().isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }

        AccountId accountId = AccountId.of(command.accountId());
        EventId eventId = EventId.of(command.eventId());

        eventLoader.loadPublished(command.eventId());

        if (!favoriteRepository.exists(accountId, eventId)) {
            favoriteRepository.save(FavoriteEvent.create(accountId, eventId));
        }
    }
}
