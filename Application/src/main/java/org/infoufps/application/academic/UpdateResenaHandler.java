package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.academic.UpdateResenaCommand;
import org.infoufps.application.academic.ResenaDto;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.academic.Resena;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.ResourceNotFoundException;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.ResenaId;

@ApplicationScoped
public class UpdateResenaHandler {

    private final IResenaRepository resenaRepository;
    private final IAccountRepository accountRepository;

    @Inject
    public UpdateResenaHandler(IResenaRepository resenaRepository, IAccountRepository accountRepository) {
        this.resenaRepository = resenaRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public ResenaDto execute(UpdateResenaCommand command) {
        ResenaId resenaId = ResenaId.of(command.resenaId());
        AccountId autorId = AccountId.of(command.autorId());

        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la reseña con ID: " + command.resenaId()));

        if (!resena.getAutorId().equals(autorId)) {
            throw new ForbiddenException("Solo el autor puede editar su reseña");
        }

        resena.updateByAuthor(command.calificacion(), command.comentario(), command.materiaCursada(), autorId);
        resenaRepository.save(resena);

        String autorNombre = accountRepository.findById(autorId).map(Account::getName).orElse("Usuario");

        return ResenaDto.of(resena, autorNombre);
    }
}

