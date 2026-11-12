package org.infoufps.domain.calendar;

import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.FavoriteEvent;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.util.List;
import java.util.Optional;

/**
 * Puerto del repositorio de favoritos de eventos.
 */
public interface IFavoriteEventRepository {

    /**
     * Guarda un favorito. La operación es idempotente por la pareja cuenta-evento.
     *
     * @param favorite favorito a persistir.
     * @return favorito persistido.
     */
    FavoriteEvent save(FavoriteEvent favorite);

    /**
     * Busca un favorito por cuenta y evento.
     *
     * @param accountId cuenta.
     * @param eventId   evento.
     * @return favorito encontrado o vacío.
     */
    Optional<FavoriteEvent> findById(AccountId accountId, EventId eventId);

    /**
     * Verifica si existe un favorito.
     *
     * @param accountId cuenta.
     * @param eventId   evento.
     * @return true si existe.
     */
    boolean exists(AccountId accountId, EventId eventId);

    /**
     * Elimina un favorito. No falla si no existe.
     *
     * @param accountId cuenta.
     * @param eventId   evento.
     */
    void delete(AccountId accountId, EventId eventId);

    /**
     * Obtiene los identificadores de eventos publicados marcados como favoritos.
     * Los favoritos de eventos archivados se conservan pero no se listan.
     *
     * @param accountId cuenta.
     * @return identificadores visibles.
     */
    List<EventId> findPublishedEventIdsByAccount(AccountId accountId);

    /**
     * Obtiene los eventos publicados favoritos, ordenados por fecha del evento.
     *
     * @param accountId   cuenta.
     * @param pageRequest paginación.
     * @return página de eventos favoritos.
     */
    PageResult<Event> findPublishedFavoriteEvents(AccountId accountId, PageRequest pageRequest);

    /**
     * Cuenta los eventos publicados favoritos de una cuenta.
     *
     * @param accountId cuenta.
     * @return total visible.
     */
    long countPublishedByAccount(AccountId accountId);
}
