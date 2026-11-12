package org.infoufps.infrastructure.content.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.infoufps.domain.content.Content;
import org.infoufps.domain.content.ModuleType;
import org.infoufps.domain.content.IContentRepository;
import org.infoufps.domain.content.InteractionStats;
import org.infoufps.domain.calendar.Status;
import org.infoufps.infrastructure.content.persistence.ContentJpa;
import org.infoufps.infrastructure.content.persistence.InteractionStatsJpa;
import org.infoufps.domain.content.ContentId;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Adaptador de Persistencia JPA que implementa el contrato del repositorio de Dominio {@link IContentRepository}.
 * <p>
 * Gestiona el ciclo de vida de persistencia en la base de datos relacional para la entidad {@link Content},
 * ejecutando las transformaciones de datos requeridas entre el Modelo de Dominio y la Entidad de Persistencia {@link ContentJpa}.
 */
@ApplicationScoped
public class ContentRepositoryJpa implements IContentRepository {

    /** Contexto de persistencia inyectado por el contenedor de Jakarta EE. */
    @Inject
    EntityManager entityManager;

    /**
     * Persiste o actualiza una tarjeta de contenido en la base de datos.
     * Mapea la entidad de dominio a su equivalente JPA y ejecuta una operación {@code merge}.
     *
     * @param content La entidad de dominio Content a guardar.
     * @return El objeto Content original de dominio devuelto para mantener la fluidez en la capa superior.
     */
    @Override
    @Transactional
    public Content save(Content content) {
        ContentJpa existing = entityManager.find(ContentJpa.class, content.getId().value());
        if (existing != null) {
            existing.setTitle(content.getTitle());
            existing.setSubtitle(content.getSubtitle());
            existing.setDescription(content.getDescription());
            existing.setImageUrl(content.getImageUrl());
            existing.setCategoryTag(content.getCategoryTag());
            existing.setTags(content.getTags() != null ? new LinkedHashSet<>(content.getTags()) : new LinkedHashSet<>());
            existing.setTargetUrl(content.getTargetUrl());
            existing.setStatus(content.getStatus());
            existing.setModuleType(content.getModuleType());
            if (content.getStats() != null) {
                existing.getStats().setLikesCount(content.getStats().getLikesCount());
                existing.getStats().setCommentsCount(content.getStats().getCommentsCount());
            }
            return content;
        }
        ContentJpa contentJpa = toEntity(content);
        entityManager.persist(contentJpa);
        return content;
    }

    /**
     * Busca una tarjeta de contenido por su identificador de dominio (ContentId).
     *
     * @param id El Value Object ContentId a consultar.
     * @return Un {@link Optional} que contiene la entidad de dominio si existe, o vacío de lo contrario.
     */
    @Override
    public Optional<Content> findById(ContentId id) {
        ContentJpa entity = entityManager.find(ContentJpa.class, id.value());
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    /**
     * Recupera todos los contenidos que se encuentran en estado {@link Status#PUBLISHED}.
     * Metodo orientado al consumo de las tarjetas visibles en el feed público.
     *
     * @return Lista de contenidos de dominio publicados.
     */
    @Override
    public List<Content> findAllPublished() {
        return findByStatus(Status.PUBLISHED);
    }

    /**
     * Consulta contenidos filtrando por su estado dentro del ciclo de vida (UNDER_REVIEW, PUBLISHED, ARCHIVED).
     * Los resultados son ordenados alfabéticamente por título.
     *
     * @param status Estado del ciclo de vida a filtrar.
     * @return Lista de tarjetas de contenido mapeadas al dominio que coinciden con el estado especificado.
     */
    @Override
    public List<Content> findByStatus(Status status) {
        return entityManager.createQuery(
                        "SELECT DISTINCT cj FROM ContentJpa cj " +
                                "LEFT JOIN FETCH cj.tags " +
                                "WHERE cj.status = :status " +
                                "ORDER BY cj.title ASC",
                        ContentJpa.class)
                .setParameter("status", status)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Content> findPublishedByModuleType(ModuleType moduleType) {
        return entityManager.createQuery(
                        "SELECT DISTINCT cj FROM ContentJpa cj " +
                                "LEFT JOIN FETCH cj.tags " +
                                "WHERE cj.moduleType = :moduleType AND cj.status = :status " +
                                "ORDER BY cj.title ASC",
                        ContentJpa.class)
                .setParameter("moduleType", moduleType)
                .setParameter("status", Status.PUBLISHED)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Content> searchPublished(String query, int limit) {
        int max = limit > 0 ? limit : 10;
        if (query == null || query.isBlank()) {
            return entityManager.createQuery(
                            "SELECT DISTINCT cj FROM ContentJpa cj " +
                                    "LEFT JOIN FETCH cj.tags " +
                                    "WHERE cj.status = :status " +
                                    "ORDER BY cj.title ASC",
                            ContentJpa.class)
                    .setParameter("status", Status.PUBLISHED)
                    .setMaxResults(max)
                    .getResultList()
                    .stream()
                    .map(this::toDomain)
                    .toList();
        }

        String pattern = "%" + query.trim().toLowerCase() + "%";
        return entityManager.createQuery(
                        "SELECT DISTINCT cj FROM ContentJpa cj " +
                                "LEFT JOIN FETCH cj.tags " +
                                "WHERE cj.status = :status " +
                                "AND (LOWER(cj.title) LIKE :pattern OR LOWER(cj.description) LIKE :pattern) " +
                                "ORDER BY cj.title ASC",
                        ContentJpa.class)
                .setParameter("status", Status.PUBLISHED)
                .setParameter("pattern", pattern)
                .setMaxResults(max)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * Elimina físicamente el registro de la tarjeta de contenido en la base de datos a partir de su ID.
     *
     * @param id Identificador único de la tarjeta a eliminar.
     */
    @Override
    @Transactional
    public void deleteById(ContentId id) {
        entityManager.createQuery(
                        "DELETE FROM ContentJpa cj WHERE cj.contentId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    /**
     * Comprueba la existencia de una tarjeta de contenido mediante una consulta optimizada {@code COUNT}.
     * Evita la carga innecesaria de entidades y colecciones a memoria.
     *
     * @param id Identificador de la tarjeta a verificar.
     * @return {@code true} si existe al menos un registro asociado al ID; {@code false} de lo contrario.
     */
    @Override
    public boolean existsById(ContentId id) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(cj) FROM ContentJpa cj WHERE cj.contentId = :id", Long.class)
                .setParameter("id", id.value())
                .getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional
    public void incrementCommentsCount(ContentId id) {
        entityManager.createQuery(
                        "UPDATE ContentJpa c " +
                                "SET c.stats.commentsCount = c.stats.commentsCount + 1 " +
                                "WHERE c.contentId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    @Override
    @Transactional
    public void decrementCommentsCount(ContentId id) {
        entityManager.createQuery(
                        "UPDATE ContentJpa c " +
                                "SET c.stats.commentsCount = CASE WHEN c.stats.commentsCount > 0 " +
                                "THEN c.stats.commentsCount - 1 ELSE 0 END " +
                                "WHERE c.contentId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    // ── Mapeo: Dominio → JPA Entity ──────────────────────────────────

    /**
     * Mapea un objeto del Modelo de Dominio (Content) a una Entidad de Persistencia (ContentJpa).
     *
     * @param content La entidad de dominio.
     * @return Instancia mutable de ContentJpa lista para ser procesada por el EntityManager.
     */
    ContentJpa toEntity(Content content) {
        ContentJpa entity = new ContentJpa();
        entity.setContentId(content.getId().value());
        entity.setTitle(content.getTitle());
        entity.setSubtitle(content.getSubtitle());
        entity.setDescription(content.getDescription());
        entity.setImageUrl(content.getImageUrl());
        entity.setCategoryTag(content.getCategoryTag());

        if (content.getTags() != null) {
            entity.setTags(new LinkedHashSet<>(content.getTags()));
        }

        entity.setTargetUrl(content.getTargetUrl());

        if (content.getStats() != null) {
            entity.setStats(new InteractionStatsJpa(
                    content.getStats().getLikesCount(),
                    content.getStats().getCommentsCount()
            ));
        }

        entity.setStatus(content.getStatus());
        entity.setModuleType(content.getModuleType());
        return entity;
    }

    // ── Mapeo: JPA Entity → Dominio ──────────────────────────────────

    /**
     * Mapea una Entidad de Persistencia (ContentJpa) recuperada de la BD a un objeto del Modelo de Dominio (Content).
     * Garantiza la inmutabilidad y la integridad de las reglas de dominio al reconstruir el objeto mediante sus fábricas.
     *
     * @param e La entidad de persistencia recuperada por JPA.
     * @return Instancia válida de la entidad de dominio Content.
     */
    Content toDomain(ContentJpa e) {        InteractionStats stats = (e.getStats() != null)
                ? InteractionStats.of(
                        e.getStats().getLikesCount() != null ? e.getStats().getLikesCount() : 0,
                        e.getStats().getCommentsCount() != null ? e.getStats().getCommentsCount() : 0)
                : InteractionStats.empty();

        Set<String> tags = (e.getTags() != null)
                ? new LinkedHashSet<>(e.getTags())
                : new LinkedHashSet<>();

        return Content.of(
                ContentId.of(e.getContentId()),
                e.getTitle(),
                e.getSubtitle(),
                e.getDescription(),
                e.getImageUrl(),
                e.getCategoryTag(),
                tags,
                e.getTargetUrl(),
                stats,
                e.getStatus(),
                e.getModuleType()
        );
    }
}