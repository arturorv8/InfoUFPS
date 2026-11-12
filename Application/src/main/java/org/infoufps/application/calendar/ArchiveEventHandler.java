package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.calendar.EventDto;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.application.calendar.EventDtoAssembler;
import org.infoufps.domain.calendar.IEventRepository;
import org.infoufps.domain.calendar.EventId;

/**
 * Caso de uso para archivar un evento.
 */
@ApplicationScoped
public class ArchiveEventHandler {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;

    @Inject
    public ArchiveEventHandler(IEventRepository eventRepository, EventDtoAssembler assembler) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
    }

    /**
     * Archiva el evento.
     *
     * @param command identificador del evento.
     * @return evento archivado.
     */
    @Transactional
    public EventDto execute(ArchiveEventCommand command) {
        if (command == null || command.eventId() == null || command.eventId().isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        Event event = eventRepository.findById(EventId.of(command.eventId()))
                .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + command.eventId()));
        event.archive();
        eventRepository.save(event);
        return assembler.toDto(event);
    }
}
