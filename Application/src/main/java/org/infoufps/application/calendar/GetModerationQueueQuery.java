package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.calendar.ModerationCriteria;
import org.infoufps.application.calendar.EventDto;
import org.infoufps.application.calendar.ModerationEventDto;
import org.infoufps.domain.calendar.Status;
import org.infoufps.application.calendar.EventDuplicateGuard;
import org.infoufps.application.calendar.EventDtoAssembler;
import org.infoufps.application.calendar.EventQueryValidator;
import org.infoufps.domain.calendar.IEventRepository;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.util.List;

/**
 * Consulta la cola de moderación por estado, incluyendo posibles duplicados.
 */
@ApplicationScoped
public class GetModerationQueueQuery {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;
    private final EventDuplicateGuard duplicateGuard;

    @Inject
    public GetModerationQueueQuery(IEventRepository eventRepository,
                                   EventDtoAssembler assembler,
                                   EventDuplicateGuard duplicateGuard) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
        this.duplicateGuard = duplicateGuard;
    }

    /**
     * Ejecuta la consulta de la cola.
     *
     * @param rawStatus estado solicitado; por defecto UNDER_REVIEW.
     * @param page      página solicitada.
     * @param size      tamaño de página.
     * @return página de eventos con sus posibles duplicados.
     */
    public PageResult<ModerationEventDto> execute(String rawStatus, int page, int size) {
        Status status = EventQueryValidator.parseStatus(rawStatus);
        PageResult<org.infoufps.domain.calendar.Event> result = eventRepository.findByStatus(
                new ModerationCriteria(status, PageRequest.of(page, size)));

        return result.map(event -> {
            EventDto dto = assembler.toDto(event);
            List<EventDto> duplicates = assembler.toDtos(duplicateGuard.findPossibleDuplicates(event));
            return new ModerationEventDto(dto, duplicates);
        });
    }
}
