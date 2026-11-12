package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.academic.MateriaDetailDto;
import org.infoufps.application.academic.ProposeMateriaCommand;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.DuplicateResourceException;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.application.academic.DailyLimitService;
import org.infoufps.domain.account.AccountId;

@ApplicationScoped
public class ProposeMateriaHandler {

    private final IMateriaRepository materiaRepository;
    private final IAccountRepository accountRepository;
    private final DailyLimitService dailyLimitService;

    @Inject
    public ProposeMateriaHandler(IMateriaRepository materiaRepository,
                                 IAccountRepository accountRepository,
                                 DailyLimitService dailyLimitService) {
        this.materiaRepository = materiaRepository;
        this.accountRepository = accountRepository;
        this.dailyLimitService = dailyLimitService;
    }

    @Transactional
    public MateriaDetailDto execute(ProposeMateriaCommand command) {
        if (command.proponenteId() == null || command.proponenteId().isBlank()) {
            throw new DomainException("El ID del proponente es obligatorio");
        }
        AccountId proponenteId = AccountId.of(command.proponenteId());

        if (accountRepository != null && accountRepository.findById(proponenteId).isEmpty()) {
            throw new DomainException("El usuario proponente no existe");
        }

        if (dailyLimitService != null) {
            dailyLimitService.checkMateriaProposalLimit(proponenteId);
        }

        if (command.codigo() != null && !command.codigo().isBlank()) {
            String cleanCode = command.codigo().strip().toUpperCase();
            if (materiaRepository.existsByCodigo(cleanCode)) {
                throw new DuplicateResourceException("Ya existe una materia registrada con el código: " + cleanCode);
            }
        }

        Materia materia = Materia.propose(
                command.nombre(),
                command.codigo(),
                command.creditos(),
                command.tipo(),
                proponenteId
        );

        materiaRepository.save(materia);

        return MateriaDetailDto.of(materia, null, 0L);
    }
}

