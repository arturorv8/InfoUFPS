package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.academic.ProfesorDetailDto;
import org.infoufps.application.academic.ProposeProfesorCommand;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.DuplicateResourceException;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.application.academic.DailyLimitService;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.valueobjects.Email;

import java.util.List;

@ApplicationScoped
public class ProposeProfesorHandler {

    private final IProfesorRepository profesorRepository;
    private final IAccountRepository accountRepository;
    private final DailyLimitService dailyLimitService;

    @Inject
    public ProposeProfesorHandler(IProfesorRepository profesorRepository,
                                  IAccountRepository accountRepository,
                                  DailyLimitService dailyLimitService) {
        this.profesorRepository = profesorRepository;
        this.accountRepository = accountRepository;
        this.dailyLimitService = dailyLimitService;
    }

    @Transactional
    public ProfesorDetailDto execute(ProposeProfesorCommand command) {
        if (command.proponenteId() == null || command.proponenteId().isBlank()) {
            throw new DomainException("El ID del proponente es obligatorio");
        }
        AccountId proponenteId = AccountId.of(command.proponenteId());

        if (accountRepository != null && accountRepository.findById(proponenteId).isEmpty()) {
            throw new DomainException("El usuario proponente no existe");
        }

        // Límite diario
        if (dailyLimitService != null) {
            dailyLimitService.checkProfesorProposalLimit(proponenteId);
        }

        // Validación de email único si se provee
        String emailNorm = Email.normalize(command.email());
        if (emailNorm != null && profesorRepository.existsByEmail(emailNorm)) {
            throw new DuplicateResourceException("Ya existe un profesor registrado con el email: " + emailNorm);
        }

        // Validación de duplicados por nombre normalizado y facultad
        String normName = TextNormalizer.normalizeAndSortWords(command.nombre());
        if (profesorRepository.existsByNombreNormalizadoAndFacultadAndEstado(normName, command.facultad(), EstadoModeracion.PUBLICADO)) {
            throw new DuplicateResourceException("Ya existe un profesor publicado con este nombre en la facultad seleccionada");
        }
        if (profesorRepository.existsByNombreNormalizadoAndFacultadAndEstado(normName, command.facultad(), EstadoModeracion.EN_REVISION)) {
            throw new DuplicateResourceException("Ya existe una propuesta en revisión para este profesor en la facultad seleccionada");
        }

        Profesor profesor = Profesor.propose(command.nombre(), command.facultad(), emailNorm, proponenteId);
        profesorRepository.save(profesor);

        return ProfesorDetailDto.of(profesor, null, 0L, List.of(), false);
    }
}

