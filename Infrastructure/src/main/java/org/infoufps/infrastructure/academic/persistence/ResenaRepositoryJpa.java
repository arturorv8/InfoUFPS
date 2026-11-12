package org.infoufps.infrastructure.academic.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.infoufps.domain.academic.Resena;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoDestino;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.infrastructure.academic.persistence.ResenaJpa;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.MateriaId;
import org.infoufps.domain.academic.ProfesorId;
import org.infoufps.domain.academic.RatingSummary;
import org.infoufps.domain.academic.ResenaId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ResenaRepositoryJpa implements IResenaRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public Resena save(Resena resena) {
        ResenaJpa existing = entityManager.find(ResenaJpa.class, resena.getId().value());
        if (existing != null) {
            updateJpa(existing, resena);
            return resena;
        }
        ResenaJpa entity = toEntity(resena);
        entityManager.persist(entity);
        return resena;
    }

    private void updateJpa(ResenaJpa jpa, Resena domain) {
        jpa.setAutorId(domain.getAutorId().value());
        jpa.setTipoDestino(domain.getTipoDestino());
        jpa.setProfesorId(domain.getProfesorId() != null ? domain.getProfesorId().value() : null);
        jpa.setMateriaId(domain.getMateriaId() != null ? domain.getMateriaId().value() : null);
        jpa.setCalificacion(domain.getCalificacion());
        jpa.setComentario(domain.getComentario());
        jpa.setMateriaCursada(domain.getMateriaCursada());
        jpa.setEstado(domain.getEstado());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
    }

    @Override
    public Optional<Resena> findById(ResenaId id) {
        ResenaJpa entity = entityManager.find(ResenaJpa.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<Resena> findActiveByUserAndProfesor(AccountId autorId, ProfesorId profesorId) {
        return entityManager.createQuery(
                        "SELECT r FROM ResenaJpa r " +
                                "WHERE r.autorId = :autorId AND r.profesorId = :profId AND r.estado != :archived",
                        ResenaJpa.class)
                .setParameter("autorId", autorId.value())
                .setParameter("profId", profesorId.value())
                .setParameter("archived", EstadoModeracion.ARCHIVADO)
                .getResultList()
                .stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public Optional<Resena> findActiveByUserAndMateria(AccountId autorId, MateriaId materiaId) {
        return entityManager.createQuery(
                        "SELECT r FROM ResenaJpa r " +
                                "WHERE r.autorId = :autorId AND r.materiaId = :matId AND r.estado != :archived",
                        ResenaJpa.class)
                .setParameter("autorId", autorId.value())
                .setParameter("matId", materiaId.value())
                .setParameter("archived", EstadoModeracion.ARCHIVADO)
                .getResultList()
                .stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public PageResult<Resena> findPublishedByProfesor(ProfesorId profesorId, PageRequest pageRequest) {
        Long total = entityManager.createQuery(
                        "SELECT COUNT(r) FROM ResenaJpa r WHERE r.profesorId = :profId AND r.estado = :pub", Long.class)
                .setParameter("profId", profesorId.value())
                .setParameter("pub", EstadoModeracion.PUBLICADO)
                .getSingleResult();

        if (total == null || total == 0) {
            return PageResult.empty(pageRequest.page(), pageRequest.size());
        }

        List<Resena> items = entityManager.createQuery(
                        "SELECT r FROM ResenaJpa r WHERE r.profesorId = :profId AND r.estado = :pub ORDER BY r.createdAt DESC",
                        ResenaJpa.class)
                .setParameter("profId", profesorId.value())
                .setParameter("pub", EstadoModeracion.PUBLICADO)
                .setFirstResult(pageRequest.getOffset())
                .setMaxResults(pageRequest.size())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();

        return PageResult.of(items, pageRequest.page(), pageRequest.size(), total);
    }

    @Override
    public PageResult<Resena> findPublishedByMateria(MateriaId materiaId, PageRequest pageRequest) {
        Long total = entityManager.createQuery(
                        "SELECT COUNT(r) FROM ResenaJpa r WHERE r.materiaId = :matId AND r.estado = :pub", Long.class)
                .setParameter("matId", materiaId.value())
                .setParameter("pub", EstadoModeracion.PUBLICADO)
                .getSingleResult();

        if (total == null || total == 0) {
            return PageResult.empty(pageRequest.page(), pageRequest.size());
        }

        List<Resena> items = entityManager.createQuery(
                        "SELECT r FROM ResenaJpa r WHERE r.materiaId = :matId AND r.estado = :pub ORDER BY r.createdAt DESC",
                        ResenaJpa.class)
                .setParameter("matId", materiaId.value())
                .setParameter("pub", EstadoModeracion.PUBLICADO)
                .setFirstResult(pageRequest.getOffset())
                .setMaxResults(pageRequest.size())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();

        return PageResult.of(items, pageRequest.page(), pageRequest.size(), total);
    }

    @Override
    public List<Resena> findByAutorId(AccountId autorId) {
        return entityManager.createQuery(
                        "SELECT r FROM ResenaJpa r WHERE r.autorId = :autorId ORDER BY r.createdAt DESC",
                        ResenaJpa.class)
                .setParameter("autorId", autorId.value())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PageResult<Resena> searchModeration(TipoDestino destino, EstadoModeracion estado, PageRequest pageRequest) {
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(r) FROM ResenaJpa r WHERE 1=1 ");
        StringBuilder selectJpql = new StringBuilder("SELECT r FROM ResenaJpa r WHERE 1=1 ");

        if (destino != null) {
            countJpql.append(" AND r.tipoDestino = :destino");
            selectJpql.append(" AND r.tipoDestino = :destino");
        }
        if (estado != null) {
            countJpql.append(" AND r.estado = :estado");
            selectJpql.append(" AND r.estado = :estado");
        }
        selectJpql.append(" ORDER BY r.createdAt DESC");

        TypedQuery<Long> countQ = entityManager.createQuery(countJpql.toString(), Long.class);
        TypedQuery<ResenaJpa> selectQ = entityManager.createQuery(selectJpql.toString(), ResenaJpa.class);

        if (destino != null) {
            countQ.setParameter("destino", destino);
            selectQ.setParameter("destino", destino);
        }
        if (estado != null) {
            countQ.setParameter("estado", estado);
            selectQ.setParameter("estado", estado);
        }

        Long total = countQ.getSingleResult();
        if (total == null || total == 0) {
            return PageResult.empty(pageRequest.page(), pageRequest.size());
        }

        selectQ.setFirstResult(pageRequest.getOffset());
        selectQ.setMaxResults(pageRequest.size());

        List<Resena> items = selectQ.getResultList().stream().map(this::toDomain).toList();

        return PageResult.of(items, pageRequest.page(), pageRequest.size(), total);
    }

    @Override
    public Double getAverageRatingForProfesor(ProfesorId profesorId) {
        return entityManager.createQuery(
                        "SELECT AVG(r.calificacion) FROM ResenaJpa r WHERE r.profesorId = :profId AND r.estado = :pub",
                        Double.class)
                .setParameter("profId", profesorId.value())
                .setParameter("pub", EstadoModeracion.PUBLICADO)
                .getSingleResult();
    }

    @Override
    public long countPublishedByProfesor(ProfesorId profesorId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(r) FROM ResenaJpa r WHERE r.profesorId = :profId AND r.estado = :pub",
                        Long.class)
                .setParameter("profId", profesorId.value())
                .setParameter("pub", EstadoModeracion.PUBLICADO)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public Double getAverageRatingForMateria(MateriaId materiaId) {
        return entityManager.createQuery(
                        "SELECT AVG(r.calificacion) FROM ResenaJpa r WHERE r.materiaId = :matId AND r.estado = :pub",
                        Double.class)
                .setParameter("matId", materiaId.value())
                .setParameter("pub", EstadoModeracion.PUBLICADO)
                .getSingleResult();
    }

    @Override
    public long countPublishedByMateria(MateriaId materiaId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(r) FROM ResenaJpa r WHERE r.materiaId = :matId AND r.estado = :pub",
                        Long.class)
                .setParameter("matId", materiaId.value())
                .setParameter("pub", EstadoModeracion.PUBLICADO)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public Map<String, RatingSummary> getRatingSummariesByProfesor(Collection<ProfesorId> profesorIds) {
        if (profesorIds == null || profesorIds.isEmpty()) {
            return Map.of();
        }
        List<String> ids = profesorIds.stream().map(ProfesorId::value).toList();
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT r.profesorId, AVG(r.calificacion), COUNT(r) FROM ResenaJpa r " +
                                "WHERE r.profesorId IN :ids AND r.estado = :pub " +
                                "GROUP BY r.profesorId",
                        Object[].class)
                .setParameter("ids", ids)
                .setParameter("pub", EstadoModeracion.PUBLICADO)
                .getResultList();

        Map<String, RatingSummary> summaries = new HashMap<>();
        for (Object[] row : rows) {
            summaries.put(
                    (String) row[0],
                    new RatingSummary(((Number) row[1]).doubleValue(), ((Number) row[2]).longValue()));
        }
        return summaries;
    }

    @Override
    public Map<String, RatingSummary> getRatingSummariesByMateria(Collection<MateriaId> materiaIds) {
        if (materiaIds == null || materiaIds.isEmpty()) {
            return Map.of();
        }
        List<String> ids = materiaIds.stream().map(MateriaId::value).toList();
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT r.materiaId, AVG(r.calificacion), COUNT(r) FROM ResenaJpa r " +
                                "WHERE r.materiaId IN :ids AND r.estado = :pub " +
                                "GROUP BY r.materiaId",
                        Object[].class)
                .setParameter("ids", ids)
                .setParameter("pub", EstadoModeracion.PUBLICADO)
                .getResultList();

        Map<String, RatingSummary> summaries = new HashMap<>();
        for (Object[] row : rows) {
            summaries.put(
                    (String) row[0],
                    new RatingSummary(((Number) row[1]).doubleValue(), ((Number) row[2]).longValue()));
        }
        return summaries;
    }

    @Override
    public long countTodaySubmissionsByAutor(AccountId autorId) {
        LocalDateTime startOfDay = BogotaTime.today().atStartOfDay();
        Long count = entityManager.createQuery(
                        "SELECT COUNT(r) FROM ResenaJpa r WHERE r.autorId = :autorId AND r.createdAt >= :startOfDay",
                        Long.class)
                .setParameter("autorId", autorId.value())
                .setParameter("startOfDay", startOfDay)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    @Transactional
    public void deleteById(ResenaId id) {
        entityManager.createQuery("DELETE FROM ResenaJpa r WHERE r.resenaId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    // ── Mapeos ──────────────────────────────────────────────────────────

    private ResenaJpa toEntity(Resena domain) {
        ResenaJpa entity = new ResenaJpa();
        entity.setResenaId(domain.getId().value());
        entity.setAutorId(domain.getAutorId().value());
        entity.setTipoDestino(domain.getTipoDestino());
        entity.setProfesorId(domain.getProfesorId() != null ? domain.getProfesorId().value() : null);
        entity.setMateriaId(domain.getMateriaId() != null ? domain.getMateriaId().value() : null);
        entity.setCalificacion(domain.getCalificacion());
        entity.setComentario(domain.getComentario());
        entity.setMateriaCursada(domain.getMateriaCursada());
        entity.setEstado(domain.getEstado());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private Resena toDomain(ResenaJpa jpa) {
        return Resena.of(
                ResenaId.of(jpa.getResenaId()),
                AccountId.of(jpa.getAutorId()),
                jpa.getTipoDestino(),
                jpa.getProfesorId() != null ? ProfesorId.of(jpa.getProfesorId()) : null,
                jpa.getMateriaId() != null ? MateriaId.of(jpa.getMateriaId()) : null,
                jpa.getCalificacion(),
                jpa.getComentario(),
                jpa.getMateriaCursada(),
                jpa.getEstado(),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }
}

