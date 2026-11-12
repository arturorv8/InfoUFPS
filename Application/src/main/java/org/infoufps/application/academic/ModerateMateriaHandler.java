package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.academic.MateriaDetailDto;
import org.infoufps.application.academic.ModerateEntityCommand;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.shared.exceptions.DuplicateResourceException;
import org.infoufps.domain.shared.exceptions.ResourceNotFoundException;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.academic.MateriaId;

@ApplicationScoped
public class ModerateMateriaHandler {

    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    @Inject
    public ModerateMateriaHandler(IMateriaRepository materiaRepository, IResenaRepository resenaRepository) {
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    @Transactional
    public MateriaDetailDto publish(String id, ModerateEntityCommand command) {
        MateriaId materiaId = MateriaId.of(id);
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la materia con ID: " + id));

        if (command != null) {
            String nombreFinal = (command.nuevoNombre() != null && !command.nuevoNombre().isBlank())
                    ? command.nuevoNombre()
                    : materia.getNombre();
            String codigoFinal = (command.nuevoCodigo() != null && !command.nuevoCodigo().isBlank())
                    ? command.nuevoCodigo().strip().toUpperCase()
                    : materia.getCodigo();
            Integer creditosFinal = command.nuevosCreditos() != null ? command.nuevosCreditos() : materia.getCreditos();
            var tipoFinal = command.nuevoTipo() != null ? command.nuevoTipo() : materia.getTipo();

            if (codigoFinal != null && materiaRepository.existsByCodigoAndNotId(codigoFinal, materiaId)) {
                throw new DuplicateResourceException("Ya existe una materia registrada con el código: " + codigoFinal);
            }

            materia.adminUpdate(nombreFinal, codigoFinal, creditosFinal, tipoFinal);
        } else {
            if (materia.getCodigo() != null && materiaRepository.existsByCodigoAndNotId(materia.getCodigo(), materiaId)) {
                throw new DuplicateResourceException("Ya existe una materia registrada con el código: " + materia.getCodigo());
            }
        }

        materia.publish();
        materiaRepository.save(materia);

        Double avg = resenaRepository.getAverageRatingForMateria(materiaId);
        long count = resenaRepository.countPublishedByMateria(materiaId);

        return MateriaDetailDto.of(materia, avg, count);
    }

    @Transactional
    public MateriaDetailDto archive(String id) {
        MateriaId materiaId = MateriaId.of(id);
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la materia con ID: " + id));

        materia.archive();
        materiaRepository.save(materia);

        Double avg = resenaRepository.getAverageRatingForMateria(materiaId);
        long count = resenaRepository.countPublishedByMateria(materiaId);

        return MateriaDetailDto.of(materia, avg, count);
    }
}

