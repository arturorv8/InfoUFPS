package org.infoufps.domain.academic;

import org.infoufps.domain.academic.ProfesorSearchCriteria;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.ProfesorId;

import java.util.List;
import java.util.Optional;

public interface IProfesorRepository {

    Profesor save(Profesor profesor);

    Optional<Profesor> findById(ProfesorId id);

    PageResult<Profesor> search(ProfesorSearchCriteria criteria, PageRequest pageRequest);

    boolean existsByNombreNormalizadoAndFacultadAndEstado(String nombreNormalizado, Facultad facultad, EstadoModeracion estado);

    boolean existsByEmail(String email);

    boolean existsByEmailAndNotId(String email, ProfesorId excludeId);

    List<Profesor> findSimilar(String nombreNormalizado, Facultad facultad);

    List<Profesor> findByProponenteId(AccountId proponenteId);

    long countTodaySubmissionsByProponente(AccountId proponenteId);

    void deleteById(ProfesorId id);
}

