package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.academic.ResenaDto;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.academic.Resena;
import org.infoufps.domain.shared.exceptions.ResourceNotFoundException;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.academic.ResenaId;

@ApplicationScoped
public class ModerateResenaHandler {

    private final IResenaRepository resenaRepository;
    private final IAccountRepository accountRepository;

    @Inject
    public ModerateResenaHandler(IResenaRepository resenaRepository, IAccountRepository accountRepository) {
        this.resenaRepository = resenaRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public ResenaDto publish(String id) {
        ResenaId resenaId = ResenaId.of(id);
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la reseña con ID: " + id));

        resena.publish();
        resenaRepository.save(resena);

        String autorNombre = accountRepository.findById(resena.getAutorId()).map(Account::getName).orElse("Usuario");
        return ResenaDto.of(resena, autorNombre);
    }

    @Transactional
    public ResenaDto archive(String id) {
        ResenaId resenaId = ResenaId.of(id);
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la reseña con ID: " + id));

        resena.archive();
        resenaRepository.save(resena);

        String autorNombre = accountRepository.findById(resena.getAutorId()).map(Account::getName).orElse("Usuario");
        return ResenaDto.of(resena, autorNombre);
    }
}

