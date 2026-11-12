package org.infoufps.domain.academic;

import org.infoufps.domain.academic.Resena;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoDestino;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.MateriaId;
import org.infoufps.domain.academic.ProfesorId;
import org.infoufps.domain.academic.RatingSummary;
import org.infoufps.domain.academic.ResenaId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IResenaRepository {

    Resena save(Resena resena);

    Optional<Resena> findById(ResenaId id);

    Optional<Resena> findActiveByUserAndProfesor(AccountId autorId, ProfesorId profesorId);

    Optional<Resena> findActiveByUserAndMateria(AccountId autorId, MateriaId materiaId);

    PageResult<Resena> findPublishedByProfesor(ProfesorId profesorId, PageRequest pageRequest);

    PageResult<Resena> findPublishedByMateria(MateriaId materiaId, PageRequest pageRequest);

    List<Resena> findByAutorId(AccountId autorId);

    PageResult<Resena> searchModeration(TipoDestino destino, EstadoModeracion estado, PageRequest pageRequest);

    Double getAverageRatingForProfesor(ProfesorId profesorId);

    long countPublishedByProfesor(ProfesorId profesorId);

    Double getAverageRatingForMateria(MateriaId materiaId);

    long countPublishedByMateria(MateriaId materiaId);

    /**
     * Obtiene el promedio y el total de reseñas publicadas para un conjunto de
     * profesores en una sola consulta agrupada.
     *
     * @param profesorIds identificadores de profesores.
     * @return mapa indexado por el valor del identificador del profesor.
     */
    Map<String, RatingSummary> getRatingSummariesByProfesor(Collection<ProfesorId> profesorIds);

    /**
     * Obtiene el promedio y el total de reseñas publicadas para un conjunto de
     * materias en una sola consulta agrupada.
     *
     * @param materiaIds identificadores de materias.
     * @return mapa indexado por el valor del identificador de la materia.
     */
    Map<String, RatingSummary> getRatingSummariesByMateria(Collection<MateriaId> materiaIds);

    long countTodaySubmissionsByAutor(AccountId autorId);

    void deleteById(ResenaId id);
}

