package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.application.academic.ProfesorDetailDto;
import org.infoufps.application.academic.UpdateProposedProfesorCommand;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.DuplicateResourceException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.ResourceNotFoundException;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.valueobjects.Email;
import org.infoufps.domain.academic.ProfesorId;

import java.util.List;

@ApplicationScoped
public class UpdateProposedProfesorHandler {

    private final IProfesorRepository profesorRepository;

    @Inject
    public UpdateProposedProfesorHandler(IProfesorRepository profesorRepository) {
        this.profesorRepository = profesorRepository;
    }

    @Transactional
    public ProfesorDetailDto execute(UpdateProposedProfesorCommand command) {
        ProfesorId profesorId = ProfesorId.of(command.profesorId());
        AccountId editorId = AccountId.of(command.editorId());

        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el profesor con ID: " + command.profesorId()));

        if (profesor.getEstado() != EstadoModeracion.EN_REVISION) {
            throw new DomainException("Solo se pueden editar propuestas en estado de revisión");
        }

        if (profesor.getProponenteId() == null || !profesor.getProponenteId().equals(editorId)) {
            throw new ForbiddenException("No tienes permiso para editar esta propuesta");
        }

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

        profesor.updateProposal(command.nombre(), command.facultad(), emailNorm, editorId);
        profesorRepository.save(profesor);

        return ProfesorDetailDto.of(profesor, null, 0L, List.of(), false);
    }
}
