package org.infoufps.domain.academic;

import org.infoufps.domain.academic.MateriaSearchCriteria;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.MateriaId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IMateriaRepository {

    Materia save(Materia materia);

    Optional<Materia> findById(MateriaId id);

    Optional<Materia> findByCodigo(String codigo);

    PageResult<Materia> search(MateriaSearchCriteria criteria, PageRequest pageRequest);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndNotId(String codigo, MateriaId excludeId);

    /**
     * Retorna candidatos para validación de similitud en memoria.
     * Limitación: Se compara en memoria; para grandes volúmenes se recomienda pg_trgm en SQL.
     */
    List<Materia> findSimilar();

    List<Materia> findByProponenteId(AccountId proponenteId);

    long countTodaySubmissionsByProponente(AccountId proponenteId);

    List<Materia> findByIds(Collection<MateriaId> ids);

    void deleteById(MateriaId id);
}

