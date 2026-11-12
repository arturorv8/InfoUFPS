package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.calendar.EventDto;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.application.calendar.EventDuplicateGuard;
import org.infoufps.application.calendar.EventDtoAssembler;
import org.infoufps.domain.calendar.IEventRepository;
import org.infoufps.domain.calendar.EventId;

/**
 * Caso de uso para publicar un evento en revisión.
 */
@ApplicationScoped
public class PublishEventHandler {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;
    private final EventDuplicateGuard duplicateGuard;

    @Inject
    public PublishEventHandler(IEventRepository eventRepository,
                               EventDtoAssembler assembler,
                               EventDuplicateGuard duplicateGuard) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
        this.duplicateGuard = duplicateGuard;
    }

    /**
     * Publica el evento tras comprobar que no exista otro evento publicado con
     * el mismo título, fecha y hora.
     *
     * @param command identificador del evento.
     * @return evento publicado.
     */
    @Transactional
    public EventDto execute(PublishEventCommand command) {
        if (command == null || command.eventId() == null || command.eventId().isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        Event event = eventRepository.findById(EventId.of(command.eventId()))
                .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + command.eventId()));

        duplicateGuard.ensureNoPublishedDuplicate(
                event.getTitle(),
                event.getStartDate(),
                event.getStartTime(),
                event.getId());

        event.publish();
        eventRepository.save(event);
        return assembler.toDto(event);
    }
}
