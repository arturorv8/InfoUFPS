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
import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.DuplicateResourceException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.shared.exceptions.ResourceNotFoundException;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.shared.valueobjects.Email;
import org.infoufps.domain.academic.MateriaId;
import org.infoufps.domain.academic.ProfesorId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AdminUpdateProfesorHandler {

    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    @Inject
    public AdminUpdateProfesorHandler(IProfesorRepository profesorRepository,
                                      IMateriaRepository materiaRepository,
                                      IResenaRepository resenaRepository) {
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    @Transactional
    public ProfesorDetailDto execute(String id, AdminSaveProfesorCommand command) {
        ProfesorId profesorId = ProfesorId.of(id);
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el profesor con ID: " + id));

        Facultad facultadEfectiva = command.facultad() != null
                ? command.facultad()
                : (profesor.getFacultad() != null ? profesor.getFacultad() : Facultad.NO_ESPECIFICADA);

        String emailNorm = Email.normalize(command.email());
        if (emailNorm != null && profesorRepository.existsByEmailAndNotId(emailNorm, profesorId)) {
            throw new DuplicateResourceException("Ya existe un profesor registrado con el email: " + emailNorm);
        }

        String normName = TextNormalizer.normalizeAndSortWords(command.nombre());
        if (!normName.equals(profesor.getNombreNormalizado()) || facultadEfectiva != profesor.getFacultad()) {
            if (profesorRepository.existsByNombreNormalizadoAndFacultadAndEstado(normName, facultadEfectiva, EstadoModeracion.PUBLICADO)) {
                throw new DuplicateResourceException("Ya existe un profesor publicado con este nombre en la facultad seleccionada");
            }
        }

        profesor.adminUpdate(command.nombre(), command.facultad(), emailNorm);

        List<Materia> materias;
        if (command.materiasIds() != null) {
            Set<MateriaId> matIds = command.materiasIds().stream().map(MateriaId::of).collect(Collectors.toSet());
            materias = validateMaterias(matIds);
            profesor.setMaterias(matIds);
        } else {
            materias = materiaRepository.findByIds(profesor.getMaterias());
        }

        profesorRepository.save(profesor);

        Double avg = resenaRepository.getAverageRatingForProfesor(profesorId);
        long count = resenaRepository.countPublishedByProfesor(profesorId);
        List<MateriaSimpleDto> materiaDtos = materias.stream().map(MateriaSimpleDto::from).toList();

        return ProfesorDetailDto.of(profesor, avg, count, materiaDtos, true);
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

