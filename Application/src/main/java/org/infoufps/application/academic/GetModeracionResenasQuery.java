package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.academic.ResenaDto;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.academic.Resena;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoDestino;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.account.AccountId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetModeracionResenasQuery {

    private final IResenaRepository resenaRepository;
    private final IAccountRepository accountRepository;

    @Inject
    public GetModeracionResenasQuery(IResenaRepository resenaRepository, IAccountRepository accountRepository) {
        this.resenaRepository = resenaRepository;
        this.accountRepository = accountRepository;
    }

    public PageResult<ResenaDto> execute(TipoDestino destino, EstadoModeracion estado, PageRequest pageRequest) {
        PageResult<Resena> result = resenaRepository.searchModeration(destino, estado, pageRequest);

        List<AccountId> autorIds = result.items().stream()
                .map(Resena::getAutorId)
                .distinct()
                .toList();
        Map<String, String> nombres = accountRepository.findByIds(autorIds).stream()
                .collect(Collectors.toMap(
                        account -> account.getAccountId().value(),
                        Account::getName,
                        (a, b) -> a));

        List<ResenaDto> dtos = result.items().stream()
                .map(r -> ResenaDto.of(r, nombres.getOrDefault(r.getAutorId().value(), "Usuario")))
                .toList();

        return PageResult.of(dtos, result.page(), result.size(), result.totalElements());
    }
}
