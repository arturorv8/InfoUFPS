package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.calendar.EventDto;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.shared.exceptions.DailyLimitExceededException;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.application.calendar.EventDuplicateGuard;
import org.infoufps.application.calendar.EventDtoAssembler;
import org.infoufps.application.calendar.CalendarSettings;
import org.infoufps.domain.calendar.IEventRepository;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventSchedule;

/**
 * Caso de uso para que un usuario proponga un evento.
 * La propuesta entra en estado UNDER_REVIEW y queda sujeta al límite diario.
 */
@ApplicationScoped
public class ProposeEventHandler {

    private final IEventRepository eventRepository;
    private final EventDuplicateGuard duplicateGuard;
    private final EventDtoAssembler assembler;
    private final CalendarSettings settings;

    @Inject
    public ProposeEventHandler(IEventRepository eventRepository,
                               EventDuplicateGuard duplicateGuard,
                               EventDtoAssembler assembler,
                               CalendarSettings settings) {
        this.eventRepository = eventRepository;
        this.duplicateGuard = duplicateGuard;
        this.assembler = assembler;
        this.settings = settings;
    }

    /**
     * Ejecuta la propuesta de un evento.
     *
     * @param command datos de la propuesta.
     * @return evento creado en revisión.
     */
    @Transactional
    public EventDto execute(ProposeEventCommand command) {
        if (command == null || command.data() == null) {
            throw new DomainException("Los datos del evento son obligatorios");
        }
        if (command.accountId() == null || command.accountId().isBlank()) {
            throw new DomainException("La cuenta es obligatoria para proponer un evento");
        }

        AccountId accountId = AccountId.of(command.accountId());
        EventRequest data = command.data();
        EventSchedule schedule = data.toSchedule();

        Event event = Event.create(
                data.title(),
                data.toEventType(),
                data.location(),
                schedule,
                data.description(),
                data.infoUrl(),
                accountId,
                false
        );

        duplicateGuard.ensureNoPublishedDuplicate(
                event.getTitle(), event.getStartDate(), event.getStartTime(), event.getId());

        int limit = settings != null ? settings.dailyProposalLimit() : 5;
        long proposalsToday = eventRepository.countProposalsByAccountOnDate(accountId, BogotaTime.today());
        if (proposalsToday >= limit) {
            throw new DailyLimitExceededException("Has alcanzado el límite diario de propuestas (" + limit + ")");
        }

        eventRepository.save(event);
        return assembler.toDto(event);
    }
}
