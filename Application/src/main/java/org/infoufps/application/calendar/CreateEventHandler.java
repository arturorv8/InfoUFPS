package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.calendar.EventDto;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.application.calendar.EventDuplicateGuard;
import org.infoufps.application.calendar.EventDtoAssembler;
import org.infoufps.domain.calendar.IEventRepository;
import org.infoufps.domain.account.AccountId;

/**
 * Caso de uso para que un administrador cree un evento.
 * El evento se crea publicado y con fecha de publicación inmediata.
 */
@ApplicationScoped
public class CreateEventHandler {

    private final IEventRepository eventRepository;
    private final EventDuplicateGuard duplicateGuard;
    private final EventDtoAssembler assembler;

    @Inject
    public CreateEventHandler(IEventRepository eventRepository,
                              EventDuplicateGuard duplicateGuard,
                              EventDtoAssembler assembler) {
        this.eventRepository = eventRepository;
        this.duplicateGuard = duplicateGuard;
        this.assembler = assembler;
    }

    /**
     * Ejecuta la creación administrativa de un evento.
     *
     * @param command datos del evento y cuenta administradora.
     * @param accountId cuenta que crea.
     * @return evento publicado.
     */
    @Transactional
    public EventDto execute(CreateEventCommand command, String accountId) {
        if (command == null || command.data() == null) {
            throw new DomainException("Los datos del evento son obligatorios");
        }
        if (accountId == null || accountId.isBlank()) {
            throw new DomainException("La cuenta administradora es obligatoria");
        }

        EventRequest data = command.data();
        Event event = Event.create(
                data.title(),
                data.toEventType(),
                data.location(),
                data.toSchedule(),
                data.description(),
                data.infoUrl(),
                AccountId.of(accountId),
                true
        );

        duplicateGuard.ensureNoPublishedDuplicate(
                event.getTitle(), event.getStartDate(), event.getStartTime(), event.getId());

        event.publish();
        eventRepository.save(event);
        return assembler.toDto(event);
    }
}
