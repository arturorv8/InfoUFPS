package org.infoufps.domain.calendar;

import org.infoufps.domain.calendar.EventDuplicateCriteria;
import org.infoufps.domain.calendar.EventPageCriteria;
import org.infoufps.domain.calendar.EventRangeCriteria;
import org.infoufps.domain.calendar.ModerationCriteria;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Puerto del repositorio de eventos del calendario.
 */
public interface IEventRepository {

    /**
     * Guarda o actualiza un evento.
     *
     * @param event evento a persistir.
     * @return evento persistido.
     */
    Event save(Event event);

    /**
     * Busca un evento por su identificador.
     *
     * @param id identificador del evento.
     * @return evento encontrado o vacío.
     */
    Optional<Event> findById(EventId id);

    /**
     * Obtiene eventos publicados que se solapan con el rango solicitado.
     * Un evento se solapa si su inicio es anterior o igual al fin del rango y su
     * fin es posterior o igual al inicio del rango.
     *
     * @param criteria rango y filtro de tipos.
     * @return eventos publicados ordenados para presentación.
     */
    List<Event> findPublishedOverlapping(EventRangeCriteria criteria);

    /**
     * Obtiene eventos publicados próximos o en curso (fin mayor o igual a hoy).
     *
     * @param criteria tipos, fecha de referencia y paginación.
     * @return página de eventos.
     */
    PageResult<Event> findPublishedUpcomingOrOngoing(EventPageCriteria criteria);

    /**
     * Obtiene las propuestas de una cuenta, sin importar su estado.
     *
     * @param accountId   cuenta proponente.
     * @param pageRequest paginación.
     * @return página de propuestas.
     */
    PageResult<Event> findByProposedBy(AccountId accountId, PageRequest pageRequest);

    /**
     * Obtiene la cola de moderación por estado.
     *
     * @param criteria estado y paginación.
     * @return página de eventos.
     */
    PageResult<Event> findByStatus(ModerationCriteria criteria);

    /**
     * Busca posibles duplicados por título, fecha y hora de inicio.
     * Excluye el evento indicado y los archivados.
     *
     * @param criteria criterio de duplicidad.
     * @return eventos candidatos.
     */
    List<Event> findDuplicates(EventDuplicateCriteria criteria);

    /**
     * Cuenta las propuestas creadas por una cuenta en una fecha local.
     *
     * @param accountId cuenta proponente.
     * @param date      fecha de creación en America/Bogota.
     * @return cantidad de propuestas.
     */
    long countProposalsByAccountOnDate(AccountId accountId, LocalDate date);

    /**
     * Elimina físicamente un evento.
     *
     * @param id identificador del evento.
     */
    void deleteById(EventId id);

    /**
     * Verifica la existencia de un evento.
     *
     * @param id identificador del evento.
     * @return true si existe.
     */
    boolean existsById(EventId id);
}
