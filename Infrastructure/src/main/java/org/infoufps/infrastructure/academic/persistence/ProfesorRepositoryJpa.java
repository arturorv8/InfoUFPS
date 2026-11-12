package org.infoufps.infrastructure.academic.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import org.infoufps.domain.academic.ProfesorSearchCriteria;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.infrastructure.academic.persistence.MateriaJpa;
import org.infoufps.infrastructure.academic.persistence.ProfesorJpa;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.MateriaId;
import org.infoufps.domain.academic.ProfesorId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProfesorRepositoryJpa implements IProfesorRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public Profesor save(Profesor profesor) {
        ProfesorJpa existing = entityManager.find(ProfesorJpa.class, profesor.getId().value());
        if (existing != null) {
            updateJpa(existing, profesor);
            return profesor;
        }
        ProfesorJpa entity = toEntity(profesor);
        entityManager.persist(entity);
        return profesor;
    }

    private void updateJpa(ProfesorJpa jpa, Profesor domain) {
        jpa.setNombre(domain.getNombre());
        jpa.setNombreNormalizado(domain.getNombreNormalizado());
        jpa.setFacultad(domain.getFacultad());
        jpa.setEmail(domain.getEmail());
        jpa.setEstado(domain.getEstado());
        jpa.setProponenteId(domain.getProponenteId() != null ? domain.getProponenteId().value() : null);
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
    }

    @Override
    public Optional<Profesor> findById(ProfesorId id) {
        ProfesorJpa entity = entityManager.find(ProfesorJpa.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public PageResult<Profesor> search(ProfesorSearchCriteria criteria, PageRequest pageRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // 1. Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProfesorJpa> countRoot = countQuery.from(ProfesorJpa.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, criteria);
        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PageResult.empty(pageRequest.page(), pageRequest.size());
        }

        // 2. Select query
        CriteriaQuery<ProfesorJpa> selectQuery = cb.createQuery(ProfesorJpa.class);
        Root<ProfesorJpa> root = selectQuery.from(ProfesorJpa.class);
        List<Predicate> predicates = buildPredicates(cb, root, criteria);

        selectQuery.where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.asc(root.get("nombre")));

        TypedQuery<ProfesorJpa> query = entityManager.createQuery(selectQuery);
        query.setFirstResult(pageRequest.getOffset());
        query.setMaxResults(pageRequest.size());

        List<Profesor> items = query.getResultList().stream().map(this::toDomainSummary).toList();

        return PageResult.of(items, pageRequest.page(), pageRequest.size(), totalElements);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<ProfesorJpa> root, ProfesorSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.estado() != null) {
            predicates.add(cb.equal(root.get("estado"), criteria.estado()));
        }

        if (criteria.facultad() != null) {
            predicates.add(cb.equal(root.get("facultad"), criteria.facultad()));
        }

        if (criteria.query() != null && !criteria.query().isBlank()) {
            List<String> tokens = TextNormalizer.tokenize(criteria.query());
            for (String token : tokens) {
                String escaped = TextNormalizer.escapeLikePattern(token);
                predicates.add(cb.like(root.get("nombreNormalizado"), "%" + escaped + "%", '\\'));
            }
        }

        return predicates;
    }

    @Override
    public boolean existsByNombreNormalizadoAndFacultadAndEstado(String nombreNormalizado, Facultad facultad, EstadoModeracion estado) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(p) FROM ProfesorJpa p " +
                                "WHERE p.nombreNormalizado = :norm AND p.facultad = :facultad AND p.estado = :estado", Long.class)
                .setParameter("norm", nombreNormalizado)
                .setParameter("facultad", facultad)
                .setParameter("estado", estado)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        Long count = entityManager.createQuery(
                        "SELECT COUNT(p) FROM ProfesorJpa p WHERE LOWER(p.email) = :email", Long.class)
                .setParameter("email", email.trim().toLowerCase())
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmailAndNotId(String email, ProfesorId excludeId) {
        if (email == null || email.isBlank()) {
            return false;
        }
        Long count = entityManager.createQuery(
                        "SELECT COUNT(p) FROM ProfesorJpa p WHERE LOWER(p.email) = :email AND p.profesorId != :excludeId", Long.class)
                .setParameter("email", email.trim().toLowerCase())
                .setParameter("excludeId", excludeId.value())
                .getSingleResult();
        return count != null && count > 0;
    }

    /**
     * Retorna candidatos de profesores para validación de similitud en memoria.
     * LIMITACIÓN: El filtrado fino por distancia Levenshtein se ejecuta en memoria sobre
     * candidatos prefiltrados por facultad y limitados a 100 resultados.
     * En producción con catálogos grandes, migrar a índices trigram (pg_trgm) en PostgreSQL.
     */
    @Override
    public List<Profesor> findSimilar(String nombreNormalizado, Facultad facultad) {
        String jpql = "SELECT p FROM ProfesorJpa p WHERE p.estado != :archived";
        if (facultad != null && facultad != Facultad.NO_ESPECIFICADA) {
            jpql += " AND p.facultad = :facultad";
        }
        TypedQuery<ProfesorJpa> q = entityManager.createQuery(jpql, ProfesorJpa.class)
                .setParameter("archived", EstadoModeracion.ARCHIVADO);
        if (facultad != null && facultad != Facultad.NO_ESPECIFICADA) {
            q.setParameter("facultad", facultad);
        }
        return q.setMaxResults(100).getResultList().stream().map(this::toDomainSummary).toList();
    }

    @Override
    public List<Profesor> findByProponenteId(AccountId proponenteId) {
        return entityManager.createQuery(
                        "SELECT p FROM ProfesorJpa p WHERE p.proponenteId = :propId ORDER BY p.createdAt DESC",
                        ProfesorJpa.class)
                .setParameter("propId", proponenteId.value())
                .getResultList()
                .stream()
                .map(this::toDomainSummary)
                .toList();
    }

    @Override
    public long countTodaySubmissionsByProponente(AccountId proponenteId) {
        LocalDateTime startOfDay = BogotaTime.today().atStartOfDay();
        Long count = entityManager.createQuery(
                        "SELECT COUNT(p) FROM ProfesorJpa p WHERE p.proponenteId = :propId AND p.createdAt >= :startOfDay",
                        Long.class)
                .setParameter("propId", proponenteId.value())
                .setParameter("startOfDay", startOfDay)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    @Transactional
    public void deleteById(ProfesorId id) {
        entityManager.createQuery("DELETE FROM ProfesorJpa p WHERE p.profesorId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    // ── Mapeos ──────────────────────────────────────────────────────────

    private ProfesorJpa toEntity(Profesor domain) {
        ProfesorJpa entity = new ProfesorJpa();
        entity.setProfesorId(domain.getId().value());
        entity.setNombre(domain.getNombre());
        entity.setNombreNormalizado(domain.getNombreNormalizado());
        entity.setFacultad(domain.getFacultad());
        entity.setEmail(domain.getEmail());
        entity.setEstado(domain.getEstado());
        entity.setProponenteId(domain.getProponenteId() != null ? domain.getProponenteId().value() : null);
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        if (domain.getMaterias() != null && !domain.getMaterias().isEmpty()) {
            Set<MateriaJpa> materiasJpa = domain.getMaterias().stream()
                    .map(mId -> entityManager.find(MateriaJpa.class, mId.value()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            entity.setMaterias(materiasJpa);
        } else {
            entity.setMaterias(new LinkedHashSet<>());
        }

        return entity;
    }

    private Profesor toDomain(ProfesorJpa jpa) {
        Set<MateriaId> materiasIds = new LinkedHashSet<>();
        if (jpa.getMaterias() != null) {
            jpa.getMaterias().forEach(m -> materiasIds.add(MateriaId.of(m.getMateriaId())));
        }

        return toDomain(jpa, materiasIds);
    }

    /**
     * Mapea un profesor sin tocar la colección LAZY de materias. Se usa en
     * listados y búsquedas donde las materias no forman parte del DTO, evitando
     * una consulta adicional por cada fila.
     */
    private Profesor toDomainSummary(ProfesorJpa jpa) {
        return toDomain(jpa, Set.of());
    }

    private Profesor toDomain(ProfesorJpa jpa, Set<MateriaId> materiasIds) {
        return Profesor.of(
                ProfesorId.of(jpa.getProfesorId()),
                jpa.getNombre(),
                jpa.getNombreNormalizado(),
                jpa.getFacultad(),
                jpa.getEmail(),
                jpa.getEstado(),
                jpa.getProponenteId() != null ? AccountId.of(jpa.getProponenteId()) : null,
                jpa.getCreatedAt(),
                jpa.getUpdatedAt(),
                materiasIds
        );
    }
}

