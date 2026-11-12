package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.academic.MateriaSimpleDto;
import org.infoufps.application.academic.PublicProfesorDetailDto;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.shared.exceptions.ResourceNotFoundException;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.ProfesorId;

import java.util.List;

@ApplicationScoped
public class GetProfesorDetailQuery {

    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    @Inject
    public GetProfesorDetailQuery(IProfesorRepository profesorRepository,
                                  IMateriaRepository materiaRepository,
                                  IResenaRepository resenaRepository) {
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    public PublicProfesorDetailDto execute(String profesorIdStr, AccountId requesterId, boolean isAdmin) {
        ProfesorId profesorId = ProfesorId.of(profesorIdStr);
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el profesor con ID: " + profesorIdStr));

        boolean isAuthor = requesterId != null && requesterId.equals(profesor.getProponenteId());

        if (profesor.getEstado() != EstadoModeracion.PUBLICADO && !isAdmin && !isAuthor) {
            throw new ResourceNotFoundException("El profesor no está disponible o no ha sido publicado");
        }

        Double avg = resenaRepository.getAverageRatingForProfesor(profesorId);
        long total = resenaRepository.countPublishedByProfesor(profesorId);
        List<Materia> materias = materiaRepository.findByIds(profesor.getMaterias());
        List<MateriaSimpleDto> materiaDtos = materias.stream().map(MateriaSimpleDto::from).toList();

        return PublicProfesorDetailDto.of(profesor, avg, total, materiaDtos);
    }
}
