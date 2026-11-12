package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.academic.PublicMateriaDetailDto;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.shared.exceptions.ResourceNotFoundException;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.MateriaId;

@ApplicationScoped
public class GetMateriaDetailQuery {

    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;

    @Inject
    public GetMateriaDetailQuery(IMateriaRepository materiaRepository, IResenaRepository resenaRepository) {
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
    }

    public PublicMateriaDetailDto execute(String materiaIdStr, AccountId requesterId, boolean isAdmin) {
        MateriaId materiaId = MateriaId.of(materiaIdStr);
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la materia con ID: " + materiaIdStr));

        boolean isAuthor = requesterId != null && requesterId.equals(materia.getProponenteId());

        if (materia.getEstado() != EstadoModeracion.PUBLICADO && !isAdmin && !isAuthor) {
            throw new ResourceNotFoundException("La materia no está disponible o no ha sido publicada");
        }

        Double avg = resenaRepository.getAverageRatingForMateria(materiaId);
        long total = resenaRepository.countPublishedByMateria(materiaId);

        return PublicMateriaDetailDto.of(materia, avg, total);
    }
}
