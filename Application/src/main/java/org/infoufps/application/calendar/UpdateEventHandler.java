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
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;

/**
 * Caso de uso para editar un evento o corregir una propuesta.
 * La revisión se incrementa y {@code publishedAt} se conserva.
 */
@ApplicationScoped
public class UpdateEventHandler {

    private final IEventRepository eventRepository;
    private final EventDuplicateGuard duplicateGuard;
    private final EventDtoAssembler assembler;

    @Inject
    public UpdateEventHandler(IEventRepository eventRepository,
                              EventDuplicateGuard duplicateGuard,
                              EventDtoAssembler assembler) {
        this.eventRepository = eventRepository;
        this.duplicateGuard = duplicateGuard;
        this.assembler = assembler;
    }

    /**
     * Ejecuta la edición.
     *
     * @param command datos de la edición.
     * @return evento actualizado.
     */
    @Transactional
    public EventDto execute(UpdateEventCommand command) {
        if (command == null || command.data() == null) {
            throw new DomainException("Los datos del evento son obligatorios");
        }
        if (command.eventId() == null || command.eventId().isBlank()) {
            throw new DomainException("El identificador del evento es obligatorio");
        }
        if (command.accountId() == null || command.accountId().isBlank()) {
            throw new DomainException("La cuenta es obligatoria para editar un evento");
        }

        Event event = eventRepository.findById(EventId.of(command.eventId()))
                .orElseThrow(() -> new NotFoundException("Evento no encontrado: " + command.eventId()));

        EventRequest data = command.data();
        event.update(
                data.title(),
                data.toEventType(),
                data.location(),
                data.toSchedule(),
                data.description(),
                data.infoUrl(),
                AccountId.of(command.accountId()),
                command.admin()
        );

        duplicateGuard.ensureNoPublishedDuplicate(
                event.getTitle(), event.getStartDate(), event.getStartTime(), event.getId());

        eventRepository.save(event);
        return assembler.toDto(event);
    }
}
