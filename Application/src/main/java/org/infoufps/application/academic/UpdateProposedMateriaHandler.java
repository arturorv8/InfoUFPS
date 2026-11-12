package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.academic.MateriaDetailDto;
import org.infoufps.application.academic.UpdateProposedMateriaCommand;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.DuplicateResourceException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.ResourceNotFoundException;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.MateriaId;

@ApplicationScoped
public class UpdateProposedMateriaHandler {

    private final IMateriaRepository materiaRepository;

    @Inject
    public UpdateProposedMateriaHandler(IMateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }

    @Transactional
    public MateriaDetailDto execute(UpdateProposedMateriaCommand command) {
        MateriaId materiaId = MateriaId.of(command.materiaId());
        AccountId editorId = AccountId.of(command.editorId());

        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la materia con ID: " + command.materiaId()));

        if (materia.getEstado() != EstadoModeracion.EN_REVISION) {
            throw new DomainException("Solo se pueden editar propuestas en revisión");
        }

        if (materia.getProponenteId() == null || !materia.getProponenteId().equals(editorId)) {
            throw new ForbiddenException("No tienes permiso para editar esta propuesta");
        }

        if (command.codigo() != null && !command.codigo().isBlank()) {
            String cleanCode = command.codigo().strip().toUpperCase();
            if (materiaRepository.existsByCodigoAndNotId(cleanCode, materiaId)) {
                throw new DuplicateResourceException("Ya existe una materia registrada con el código: " + cleanCode);
            }
        }

        materia.updateProposal(
                command.nombre(),
                command.codigo(),
                command.creditos(),
                command.tipo(),
                editorId
        );

        materiaRepository.save(materia);

        return MateriaDetailDto.of(materia, null, 0L);
    }
}

