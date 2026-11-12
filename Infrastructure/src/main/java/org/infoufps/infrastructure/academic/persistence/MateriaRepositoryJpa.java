package org.infoufps.infrastructure.academic.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import org.infoufps.domain.academic.MateriaSearchCriteria;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.infrastructure.academic.persistence.MateriaJpa;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.MateriaId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class MateriaRepositoryJpa implements IMateriaRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public Materia save(Materia materia) {
        MateriaJpa existing = entityManager.find(MateriaJpa.class, materia.getId().value());
        if (existing != null) {
            updateJpa(existing, materia);
            return materia;
        }
        MateriaJpa entity = toEntity(materia);
        entityManager.persist(entity);
        return materia;
    }

    private void updateJpa(MateriaJpa jpa, Materia domain) {
        jpa.setNombre(domain.getNombre());
        jpa.setNombreNormalizado(domain.getNombreNormalizado());
        jpa.setCodigo(domain.getCodigo());
        jpa.setCreditos(domain.getCreditos());
        jpa.setTipo(domain.getTipo());
        jpa.setEstado(domain.getEstado());
        jpa.setProponenteId(domain.getProponenteId() != null ? domain.getProponenteId().value() : null);
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
    }

    @Override
    public Optional<Materia> findById(MateriaId id) {
        MateriaJpa entity = entityManager.find(MateriaJpa.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<Materia> findByCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return entityManager.createQuery(
                        "SELECT m FROM MateriaJpa m WHERE UPPER(m.codigo) = :code", MateriaJpa.class)
                .setParameter("code", codigo.trim().toUpperCase())
                .getResultList()
                .stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public PageResult<Materia> search(MateriaSearchCriteria criteria, PageRequest pageRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // 1. Count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<MateriaJpa> countRoot = countQuery.from(MateriaJpa.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, criteria);
        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PageResult.empty(pageRequest.page(), pageRequest.size());
        }

        // 2. Select
        CriteriaQuery<MateriaJpa> selectQuery = cb.createQuery(MateriaJpa.class);
        Root<MateriaJpa> root = selectQuery.from(MateriaJpa.class);
        List<Predicate> predicates = buildPredicates(cb, root, criteria);

        selectQuery.where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.asc(root.get("nombre")));

        TypedQuery<MateriaJpa> query = entityManager.createQuery(selectQuery);
        query.setFirstResult(pageRequest.getOffset());
        query.setMaxResults(pageRequest.size());

        List<Materia> items = query.getResultList().stream().map(this::toDomain).toList();

        return PageResult.of(items, pageRequest.page(), pageRequest.size(), totalElements);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<MateriaJpa> root, MateriaSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.estado() != null) {
            predicates.add(cb.equal(root.get("estado"), criteria.estado()));
        }

        if (criteria.creditos() != null) {
            predicates.add(cb.equal(root.get("creditos"), criteria.creditos()));
        }

        if (criteria.tipo() != null) {
            predicates.add(cb.equal(root.get("tipo"), criteria.tipo()));
        }

        if (criteria.query() != null && !criteria.query().isBlank()) {
            String rawQuery = criteria.query().trim();
            // Buscar por coincidencia de código (exacta o prefijo) O por todos los tokens en nombre
            String codeCandidate = rawQuery.toUpperCase();
            Predicate codePredicate = cb.equal(cb.upper(root.get("codigo")), codeCandidate);

            List<String> tokens = TextNormalizer.tokenize(rawQuery);
            if (!tokens.isEmpty()) {
                List<Predicate> tokenPredicates = new ArrayList<>();
                for (String token : tokens) {
                    String escaped = TextNormalizer.escapeLikePattern(token);
                    tokenPredicates.add(cb.like(root.get("nombreNormalizado"), "%" + escaped + "%", '\\'));
                }
                Predicate allTokensMatch = cb.and(tokenPredicates.toArray(new Predicate[0]));
                predicates.add(cb.or(codePredicate, allTokensMatch));
            } else {
                predicates.add(codePredicate);
            }
        }

        return predicates;
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return false;
        }
        Long count = entityManager.createQuery(
                        "SELECT COUNT(m) FROM MateriaJpa m WHERE UPPER(m.codigo) = :code", Long.class)
                .setParameter("code", codigo.trim().toUpperCase())
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByCodigoAndNotId(String codigo, MateriaId excludeId) {
        if (codigo == null || codigo.isBlank()) {
            return false;
        }
        Long count = entityManager.createQuery(
                        "SELECT COUNT(m) FROM MateriaJpa m WHERE UPPER(m.codigo) = :code AND m.materiaId != :excludeId", Long.class)
                .setParameter("code", codigo.trim().toUpperCase())
                .setParameter("excludeId", excludeId.value())
                .getSingleResult();
        return count != null && count > 0;
    }

    /**
     * Retorna candidatos para análisis de similitud en memoria.
     * LIMITACIÓN: El cálculo de distancia de Levenshtein y normalización de tokens se realiza
     * en memoria sobre los candidatos activos (limitado a 100 para evitar sobrecarga).
     * En producción con catálogos >10k materias, se recomienda implementar búsqueda trigram
     * (pg_trgm) directamente en PostgreSQL.
     */
    @Override
    public List<Materia> findSimilar() {
        return entityManager.createQuery(
                        "SELECT m FROM MateriaJpa m WHERE m.estado != :archived", MateriaJpa.class)
                .setParameter("archived", EstadoModeracion.ARCHIVADO)
                .setMaxResults(100)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Materia> findByProponenteId(AccountId proponenteId) {
        return entityManager.createQuery(
                        "SELECT m FROM MateriaJpa m WHERE m.proponenteId = :propId ORDER BY m.createdAt DESC",
                        MateriaJpa.class)
                .setParameter("propId", proponenteId.value())
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countTodaySubmissionsByProponente(AccountId proponenteId) {
        LocalDateTime startOfDay = BogotaTime.today().atStartOfDay();
        Long count = entityManager.createQuery(
                        "SELECT COUNT(m) FROM MateriaJpa m WHERE m.proponenteId = :propId AND m.createdAt >= :startOfDay",
                        Long.class)
                .setParameter("propId", proponenteId.value())
                .setParameter("startOfDay", startOfDay)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public List<Materia> findByIds(Collection<MateriaId> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> idStrings = ids.stream().map(MateriaId::value).toList();
        return entityManager.createQuery(
                        "SELECT m FROM MateriaJpa m WHERE m.materiaId IN :ids ORDER BY m.nombre ASC", MateriaJpa.class)
                .setParameter("ids", idStrings)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(MateriaId id) {
        entityManager.createQuery("DELETE FROM MateriaJpa m WHERE m.materiaId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    // ── Mapeos ──────────────────────────────────────────────────────────

    private MateriaJpa toEntity(Materia domain) {
        MateriaJpa entity = new MateriaJpa();
        entity.setMateriaId(domain.getId().value());
        entity.setNombre(domain.getNombre());
        entity.setNombreNormalizado(domain.getNombreNormalizado());
        entity.setCodigo(domain.getCodigo());
        entity.setCreditos(domain.getCreditos());
        entity.setTipo(domain.getTipo());
        entity.setEstado(domain.getEstado());
        entity.setProponenteId(domain.getProponenteId() != null ? domain.getProponenteId().value() : null);
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private Materia toDomain(MateriaJpa jpa) {
        return Materia.of(
                MateriaId.of(jpa.getMateriaId()),
                jpa.getNombre(),
                jpa.getNombreNormalizado(),
                jpa.getCodigo(),
                jpa.getCreditos(),
                jpa.getTipo(),
                jpa.getEstado(),
                jpa.getProponenteId() != null ? AccountId.of(jpa.getProponenteId()) : null,
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }
}

