package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.academic.ResenaDto;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.academic.Resena;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.account.AccountId;

import java.util.List;

@ApplicationScoped
public class GetUserResenasQuery {

    private final IResenaRepository resenaRepository;
    private final IAccountRepository accountRepository;

    @Inject
    public GetUserResenasQuery(IResenaRepository resenaRepository, IAccountRepository accountRepository) {
        this.resenaRepository = resenaRepository;
        this.accountRepository = accountRepository;
    }

    public List<ResenaDto> execute(String autorIdStr) {
        AccountId autorId = AccountId.of(autorIdStr);
        String autorNombre = accountRepository.findById(autorId).map(Account::getName).orElse("Usuario");

        List<Resena> resenas = resenaRepository.findByAutorId(autorId);
        return resenas.stream()
                .map(r -> ResenaDto.of(r, autorNombre))
                .toList();
    }
}

