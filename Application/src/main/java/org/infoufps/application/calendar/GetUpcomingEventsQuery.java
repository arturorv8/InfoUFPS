package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.calendar.EventPageCriteria;
import org.infoufps.application.calendar.PublicEventDto;
import org.infoufps.application.calendar.EventDtoAssembler;
import org.infoufps.application.calendar.EventQueryValidator;
import org.infoufps.domain.calendar.IEventRepository;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.util.List;

/**
 * Consulta los eventos publicados próximos o en curso, de forma paginada.
 */
@ApplicationScoped
public class GetUpcomingEventsQuery {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;

    @Inject
    public GetUpcomingEventsQuery(IEventRepository eventRepository, EventDtoAssembler assembler) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
    }

    /**
     * Ejecuta la consulta de próximos y en curso.
     *
     * @param rawTypes tipos a filtrar.
     * @param page     página solicitada.
     * @param size     tamaño de página.
     * @return página de eventos.
     */
    public PageResult<PublicEventDto> execute(List<String> rawTypes, int page, int size) {
        EventPageCriteria criteria = new EventPageCriteria(
                EventQueryValidator.parseTypes(rawTypes),
                BogotaTime.today(),
                PageRequest.of(page, size)
        );
        return assembler.toPublicPagedResult(eventRepository.findPublishedUpcomingOrOngoing(criteria));
    }
}
