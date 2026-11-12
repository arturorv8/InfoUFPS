package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.academic.ProfesorDetailDto;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.account.AccountId;

import java.util.List;

@ApplicationScoped
public class GetProfesorPropuestasQuery {

    private final IProfesorRepository profesorRepository;

    @Inject
    public GetProfesorPropuestasQuery(IProfesorRepository profesorRepository) {
        this.profesorRepository = profesorRepository;
    }

    public List<ProfesorDetailDto> execute(String proponenteIdStr) {
        AccountId proponenteId = AccountId.of(proponenteIdStr);
        List<Profesor> profesores = profesorRepository.findByProponenteId(proponenteId);
        return profesores.stream()
                .map(p -> ProfesorDetailDto.of(p, null, 0L, List.of(), true))
                .toList();
    }
}

