package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.academic.MateriaDetailDto;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.account.AccountId;

import java.util.List;

@ApplicationScoped
public class GetMateriaPropuestasQuery {

    private final IMateriaRepository materiaRepository;

    @Inject
    public GetMateriaPropuestasQuery(IMateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }

    public List<MateriaDetailDto> execute(String proponenteIdStr) {
        AccountId proponenteId = AccountId.of(proponenteIdStr);
        List<Materia> materias = materiaRepository.findByProponenteId(proponenteId);
        return materias.stream()
                .map(m -> MateriaDetailDto.of(m, null, 0L))
                .toList();
    }
}

