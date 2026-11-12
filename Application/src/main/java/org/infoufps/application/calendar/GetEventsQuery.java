package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.calendar.EventRangeCriteria;
import org.infoufps.application.calendar.PublicEventDto;
import org.infoufps.application.calendar.EventDtoAssembler;
import org.infoufps.application.calendar.EventQueryValidator;
import org.infoufps.domain.calendar.IEventRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Consulta los eventos publicados que se solapan con un rango de fechas.
 */
@ApplicationScoped
public class GetEventsQuery {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;

    @Inject
    public GetEventsQuery(IEventRepository eventRepository, EventDtoAssembler assembler) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
    }

    /**
     * Ejecuta la consulta de rango.
     *
     * @param from     fecha inicial.
     * @param to       fecha final.
     * @param rawTypes tipos a filtrar.
     * @return eventos publicados ordenados para presentación.
     */
    public List<PublicEventDto> execute(LocalDate from, LocalDate to, List<String> rawTypes) {
        EventRangeCriteria criteria = EventQueryValidator.validateRange(from, to, rawTypes);
        return assembler.toPublicDtos(eventRepository.findPublishedOverlapping(criteria));
    }
}
