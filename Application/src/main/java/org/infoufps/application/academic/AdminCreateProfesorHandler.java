package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.academic.MateriaSimpleDto;
import org.infoufps.application.academic.ProfesorDetailDto;
import org.infoufps.application.academic.AdminSaveProfesorCommand;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.DuplicateResourceException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.shared.valueobjects.Email;
import org.infoufps.domain.academic.MateriaId;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AdminCreateProfesorHandler {

    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;

    @Inject
    public AdminCreateProfesorHandler(IProfesorRepository profesorRepository, IMateriaRepository materiaRepository) {
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
    }

    @Transactional
    public ProfesorDetailDto execute(AdminSaveProfesorCommand command) {
        String emailNorm = Email.normalize(command.email());
        if (emailNorm != null && profesorRepository.existsByEmail(emailNorm)) {
            throw new DuplicateResourceException("Ya existe un profesor registrado con el email: " + emailNorm);
        }

        String normName = TextNormalizer.normalizeAndSortWords(command.nombre());
        if (profesorRepository.existsByNombreNormalizadoAndFacultadAndEstado(normName, command.facultad(), EstadoModeracion.PUBLICADO)) {
            throw new DuplicateResourceException("Ya existe un profesor publicado con este nombre en la facultad seleccionada");
        }

        Profesor profesor = Profesor.createByAdmin(command.nombre(), command.facultad(), emailNorm);

        List<MateriaSimpleDto> materiaDtos = new ArrayList<>();
        if (command.materiasIds() != null && !command.materiasIds().isEmpty()) {
            Set<MateriaId> matIds = command.materiasIds().stream().map(MateriaId::of).collect(Collectors.toSet());
            List<Materia> materias = validateMaterias(matIds);
            profesor.setMaterias(matIds);
            materiaDtos = materias.stream().map(MateriaSimpleDto::from).toList();
        }

        profesorRepository.save(profesor);

        return ProfesorDetailDto.of(profesor, null, 0L, materiaDtos, true);
    }

    private List<Materia> validateMaterias(Set<MateriaId> matIds) {
        List<Materia> materias = materiaRepository.findByIds(matIds);
        Set<String> encontradas = materias.stream()
                .map(materia -> materia.getId().value())
                .collect(Collectors.toSet());
        List<String> inexistentes = matIds.stream()
                .map(MateriaId::value)
                .filter(id -> !encontradas.contains(id))
                .sorted()
                .toList();
        if (!inexistentes.isEmpty()) {
            throw new NotFoundException("No se encontraron las materias: " + String.join(", ", inexistentes));
        }
        List<String> noPublicadas = materias.stream()
                .filter(materia -> materia.getEstado() != EstadoModeracion.PUBLICADO)
                .map(materia -> materia.getId().value())
                .sorted()
                .toList();
        if (!noPublicadas.isEmpty()) {
            throw new DomainException("Las siguientes materias no están publicadas: " + String.join(", ", noPublicadas));
        }
        return materias;
    }
}

