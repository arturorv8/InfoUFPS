package org.infoufps.application.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.calendar.EventDto;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.application.calendar.EventDtoAssembler;
import org.infoufps.domain.calendar.IEventRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

/**
 * Consulta las propuestas de la cuenta autenticada.
 */
@ApplicationScoped
public class GetMyProposalsQuery {

    private final IEventRepository eventRepository;
    private final EventDtoAssembler assembler;

    @Inject
    public GetMyProposalsQuery(IEventRepository eventRepository, EventDtoAssembler assembler) {
        this.eventRepository = eventRepository;
        this.assembler = assembler;
    }

    /**
     * Ejecuta la consulta.
     *
     * @param accountId cuenta autenticada.
     * @param page      página solicitada.
     * @param size      tamaño de página.
     * @return página de propuestas.
     */
    public PageResult<EventDto> execute(String accountId, int page, int size) {
        if (accountId == null || accountId.isBlank()) {
            throw new DomainException("La cuenta es obligatoria");
        }
        return assembler.toPagedResult(
                eventRepository.findByProposedBy(AccountId.of(accountId), PageRequest.of(page, size)));
    }
}
