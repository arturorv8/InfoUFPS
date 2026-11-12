package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.academic.MateriaDetailDto;
import org.infoufps.application.academic.AdminSaveMateriaCommand;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.shared.exceptions.DuplicateResourceException;
import org.infoufps.domain.shared.exceptions.ResourceNotFoundException;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.academic.MateriaId;

@ApplicationScoped
public class AdminUpdateMateriaHandler {

    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    @Inject
    public AdminUpdateMateriaHandler(IMateriaRepository materiaRepository, IResenaRepository resenaRepository) {
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    @Transactional
    public MateriaDetailDto execute(String id, AdminSaveMateriaCommand command) {
        MateriaId materiaId = MateriaId.of(id);
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la materia con ID: " + id));

        if (command.codigo() != null && !command.codigo().isBlank()) {
            String cleanCode = command.codigo().strip().toUpperCase();
            if (materiaRepository.existsByCodigoAndNotId(cleanCode, materiaId)) {
                throw new DuplicateResourceException("Ya existe una materia registrada con el código: " + cleanCode);
            }
        }

        materia.adminUpdate(
                command.nombre(),
                command.codigo(),
                command.creditos(),
                command.tipo()
        );

        materiaRepository.save(materia);

        Double avg = resenaRepository.getAverageRatingForMateria(materiaId);
        long count = resenaRepository.countPublishedByMateria(materiaId);

        return MateriaDetailDto.of(materia, avg, count);
    }
}

