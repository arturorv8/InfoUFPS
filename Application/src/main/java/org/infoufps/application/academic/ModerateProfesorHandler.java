package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.academic.ProfesorDetailDto;
import org.infoufps.application.academic.ModerateEntityCommand;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.shared.exceptions.DuplicateResourceException;
import org.infoufps.domain.shared.exceptions.ResourceNotFoundException;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.shared.valueobjects.Email;
import org.infoufps.domain.academic.ProfesorId;

import java.util.List;

@ApplicationScoped
public class ModerateProfesorHandler {

    private final IProfesorRepository profesorRepository;
    private final IResenaRepository resenaRepository;

    @Inject
    public ModerateProfesorHandler(IProfesorRepository profesorRepository, IResenaRepository resenaRepository) {
        this.profesorRepository = profesorRepository;
        this.resenaRepository = resenaRepository;
    }

    @Transactional
    public ProfesorDetailDto publish(String id, ModerateEntityCommand command) {
        ProfesorId profesorId = ProfesorId.of(id);
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el profesor con ID: " + id));

        // Aplicar correcciones antes de publicar si fueron provistas
        String nombreFinal = profesor.getNombre();
        var facultadFinal = profesor.getFacultad();
        String emailFinal = profesor.getEmail();

        if (command != null) {
            if (command.nuevoNombre() != null && !command.nuevoNombre().isBlank()) {
                nombreFinal = command.nuevoNombre();
            }
            if (command.nuevaFacultad() != null) {
                facultadFinal = command.nuevaFacultad();
            }
            if (command.nuevoEmail() != null && !command.nuevoEmail().isBlank()) {
                emailFinal = Email.normalize(command.nuevoEmail());
            }
        }

        // El email debe ser único entre todos los profesores, excluyendo el propio
        if (emailFinal != null && profesorRepository.existsByEmailAndNotId(emailFinal, profesorId)) {
            throw new DuplicateResourceException("Ya existe un profesor registrado con el email: " + emailFinal);
        }

        // El nombre normalizado debe ser único entre los profesores publicados de la facultad efectiva
        String normName = TextNormalizer.normalizeAndSortWords(nombreFinal);
        if (profesorRepository.existsByNombreNormalizadoAndFacultadAndEstado(normName, facultadFinal, EstadoModeracion.PUBLICADO)) {
            throw new DuplicateResourceException("Ya existe un profesor publicado con este nombre en la facultad seleccionada");
        }

        if (command != null) {
            profesor.adminUpdate(nombreFinal, facultadFinal, emailFinal);
        }

        profesor.publish();
        profesorRepository.save(profesor);

        Double avg = resenaRepository.getAverageRatingForProfesor(profesorId);
        long count = resenaRepository.countPublishedByProfesor(profesorId);

        return ProfesorDetailDto.of(profesor, avg, count, List.of(), true);
    }

    @Transactional
    public ProfesorDetailDto archive(String id) {
        ProfesorId profesorId = ProfesorId.of(id);
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el profesor con ID: " + id));

        profesor.archive();
        profesorRepository.save(profesor);

        Double avg = resenaRepository.getAverageRatingForProfesor(profesorId);
        long count = resenaRepository.countPublishedByProfesor(profesorId);

        return ProfesorDetailDto.of(profesor, avg, count, List.of(), true);
    }
}

