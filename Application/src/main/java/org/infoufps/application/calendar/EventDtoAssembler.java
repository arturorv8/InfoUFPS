package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.calendar.EventDto;
import org.infoufps.application.calendar.PublicEventDto;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Ensambla DTOs de eventos resolviendo el nombre visible del autor.
 * El nombre es "Admin" cuando el evento fue creado por un administrador; en caso
 * contrario se usa el nombre de la cuenta. En listados se resuelven todos los
 * autores con una sola consulta.
 */
@ApplicationScoped
public class EventDtoAssembler {

    private final IAccountRepository accountRepository;

    public EventDtoAssembler() {
        this(null);
    }

    @Inject
    public EventDtoAssembler(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Convierte un evento a DTO usando la fecha actual de Bogotá.
     *
     * @param event evento de dominio.
     * @return DTO del evento.
     */
    public EventDto toDto(Event event) {
        return toDto(event, BogotaTime.today());
    }

    /**
     * Convierte un evento a DTO con una fecha de referencia.
     *
     * @param event         evento de dominio.
     * @param referenceDate fecha de referencia.
     * @return DTO del evento.
     */
    public EventDto toDto(Event event, LocalDate referenceDate) {
        Map<String, String> names = resolveAuthorNames(List.of(event));
        return toDto(event, names, referenceDate);
    }

    /**
     * Convierte una lista de eventos a DTOs.
     *
     * @param events eventos de dominio.
     * @return DTOs.
     */
    public List<EventDto> toDtos(List<Event> events) {
        Map<String, String> names = resolveAuthorNames(events);
        LocalDate referenceDate = BogotaTime.today();
        return events.stream().map(event -> toDto(event, names, referenceDate)).toList();
    }

    /**
     * Convierte una página de eventos a una página de DTOs.
     *
     * @param page página de eventos.
     * @return página de DTOs.
     */
    public PageResult<EventDto> toPagedResult(PageResult<Event> page) {
        Map<String, String> names = resolveAuthorNames(page.items());
        LocalDate referenceDate = BogotaTime.today();
        List<EventDto> dtos = page.items().stream()
                .map(event -> toDto(event, names, referenceDate))
                .toList();
        return new PageResult<>(dtos, page.page(), page.size(), page.totalElements(), page.totalPages());
    }

    /**
     * Convierte un evento a su DTO público, sin el identificador de la cuenta
     * que lo propuso.
     *
     * @param event evento de dominio.
     * @return DTO público del evento.
     */
    public PublicEventDto toPublicDto(Event event) {
        return toPublicDto(event, BogotaTime.today());
    }

    /**
     * Convierte un evento a su DTO público con una fecha de referencia.
     *
     * @param event         evento de dominio.
     * @param referenceDate fecha de referencia.
     * @return DTO público del evento.
     */
    public PublicEventDto toPublicDto(Event event, LocalDate referenceDate) {
        Map<String, String> names = resolveAuthorNames(List.of(event));
        return PublicEventDto.from(event, authorName(event, names), referenceDate);
    }

    /**
     * Convierte una lista de eventos a DTOs públicos.
     *
     * @param events eventos de dominio.
     * @return DTOs públicos.
     */
    public List<PublicEventDto> toPublicDtos(List<Event> events) {
        Map<String, String> names = resolveAuthorNames(events);
        LocalDate referenceDate = BogotaTime.today();
        return events.stream()
                .map(event -> PublicEventDto.from(event, authorName(event, names), referenceDate))
                .toList();
    }

    /**
     * Convierte una página de eventos a una página de DTOs públicos.
     *
     * @param page página de eventos.
     * @return página de DTOs públicos.
     */
    public PageResult<PublicEventDto> toPublicPagedResult(PageResult<Event> page) {
        Map<String, String> names = resolveAuthorNames(page.items());
        LocalDate referenceDate = BogotaTime.today();
        List<PublicEventDto> dtos = page.items().stream()
                .map(event -> PublicEventDto.from(event, authorName(event, names), referenceDate))
                .toList();
        return new PageResult<>(dtos, page.page(), page.size(), page.totalElements(), page.totalPages());
    }

    private EventDto toDto(Event event, Map<String, String> names, LocalDate referenceDate) {
        return EventDto.from(event, authorName(event, names), referenceDate);
    }

    private String authorName(Event event, Map<String, String> names) {
        if (event.isCreatedByAdmin()) {
            return "Admin";
        }
        if (event.getProposedBy() == null) {
            return "Usuario";
        }
        return names.getOrDefault(event.getProposedBy().value(), "Usuario");
    }

    private Map<String, String> resolveAuthorNames(List<Event> events) {
        if (accountRepository == null || events == null || events.isEmpty()) {
            return Map.of();
        }
        List<AccountId> ids = events.stream()
                .filter(event -> !event.isCreatedByAdmin())
                .map(Event::getProposedBy)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return accountRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(
                        account -> account.getAccountId().value(),
                        Account::getName,
                        (a, b) -> a));
    }
}
