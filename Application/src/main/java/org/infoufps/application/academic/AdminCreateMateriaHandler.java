package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.academic.MateriaDetailDto;
import org.infoufps.application.academic.AdminSaveMateriaCommand;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.shared.exceptions.DuplicateResourceException;
import org.infoufps.domain.academic.IMateriaRepository;

@ApplicationScoped
public class AdminCreateMateriaHandler {

    private final IMateriaRepository materiaRepository;

    @Inject
    public AdminCreateMateriaHandler(IMateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }

    @Transactional
    public MateriaDetailDto execute(AdminSaveMateriaCommand command) {
        if (command.codigo() != null && !command.codigo().isBlank()) {
            String cleanCode = command.codigo().strip().toUpperCase();
            if (materiaRepository.existsByCodigo(cleanCode)) {
                throw new DuplicateResourceException("Ya existe una materia registrada con el código: " + cleanCode);
            }
        }

        Materia materia = Materia.createByAdmin(
                command.nombre(),
                command.codigo(),
                command.creditos(),
                command.tipo()
        );

        materiaRepository.save(materia);

        return MateriaDetailDto.of(materia, null, 0L);
    }
}

