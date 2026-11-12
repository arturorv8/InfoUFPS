package org.infoufps.domain.content;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de Dominio que representa un comentario genérico realizado sobre un contenido o hilo de foro.
 * <p>
 * Reglas de negocio del modelo:
 * - Un destino polimórfico (CONTENT o FORUM_THREAD) puede albergar múltiples comentarios.
 * - Cada comentario es realizado por una cuenta/usuario específico.
 * - Un mismo usuario puede realizar múltiples comentarios dentro del mismo destino.
 * - El texto del comentario no puede ser nulo, vacío ni exceder los 1000 caracteres.
 * - Los comentarios nacen con estado {@link Status#PUBLISHED}.
 * - El borrado es lógico cambiando el estado a {@link Status#ARCHIVED}.
 */
public class Comment {

    /** Identificador único e inmutable del comentario. */
    private final CommentId id;

    /** Tipo de entidad destino a la que pertenece el comentario (CONTENT o FORUM_THREAD). */
    private final CommentTargetType targetType;

    /** Identificador de la entidad destino a la que pertenece el comentario. */
    private final String targetId;

    /** Identificador de la cuenta/usuario que realizó el comentario. */
    private final AccountId authorId;

    /** Texto del comentario. */
    private String text;

    /** Estado del ciclo de vida del comentario. */
    private Status status;

    /** Fecha y hora en la que fue publicado el comentario. */
    private final LocalDateTime createdAt;

    /** Fecha y hora de la última modificación (si ha sido editado o archivado). */
    private LocalDateTime updatedAt;

    /** Longitud máxima permitida para el texto del comentario. */
    public static final int MAX_TEXT_LENGTH = 1000;

    /**
     * Constructor privado accesible solo mediante métodos de fábrica estáticos.
     */
    private Comment(CommentId id,
                    CommentTargetType targetType,
                    String targetId,
                    AccountId authorId,
                    String text,
                    Status status,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "El id del comentario es obligatorio");
        this.targetType = Objects.requireNonNull(targetType, "El targetType es obligatorio");
        this.targetId = validateTargetId(targetId);
        this.authorId = Objects.requireNonNull(authorId, "El authorId es obligatorio");
        this.text = validateAndSanitizeText(text);
        this.status = status != null ? status : Status.PUBLISHED;
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        this.updatedAt = updatedAt;
    }

    /**
     * Fábrica para crear un nuevo comentario genérico de dominio.
     * Genera automáticamente un nuevo {@link CommentId}, asigna estado {@link Status#PUBLISHED}
     * y la fecha de creación actual.
     *
     * @param targetType Tipo de destino (CONTENT o FORUM_THREAD).
     * @param targetId   Identificador del destino que se comenta.
     * @param authorId   Identificador del usuario autor del comentario.
     * @param text       Cuerpo del comentario.
     * @return Nueva instancia de Comment válida.
     */
    public static Comment create(CommentTargetType targetType, String targetId, AccountId authorId, String text) {
        return new Comment(
                CommentId.generate(),
                targetType,
                targetId,
                authorId,
                text,
                Status.PUBLISHED,
                BogotaTime.now(),
                null
        );
    }

    /**
     * Fábrica de conveniencia para comentarios de contenidos, manteniendo retrocompatibilidad.
     */
    public static Comment create(ContentId contentId, AccountId authorId, String text) {
        Objects.requireNonNull(contentId, "El contentId es obligatorio");
        return create(CommentTargetType.CONTENT, contentId.value(), authorId, text);
    }

    /**
     * Fábrica para reconstruir un comentario desde la capa de persistencia (repositorios).
     */
    public static Comment of(CommentId id,
                             CommentTargetType targetType,
                             String targetId,
                             AccountId authorId,
                             String text,
                             Status status,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        return new Comment(id, targetType, targetId, authorId, text, status, createdAt, updatedAt);
    }

    /**
     * Fábrica de reconstrucción con firma previa para contenidos (retrocompatibilidad).
     */
    public static Comment of(CommentId id,
                             ContentId contentId,
                             AccountId authorId,
                             String text,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        return new Comment(
                id,
                CommentTargetType.CONTENT,
                contentId != null ? contentId.value() : null,
                authorId,
                text,
                Status.PUBLISHED,
                createdAt,
                updatedAt
        );
    }

    /**
     * Regla de negocio: Actualiza el texto del comentario y registra la marca de tiempo de modificación.
     *
     * @param newText El nuevo texto a asignar.
     */
    public void updateText(String newText) {
        this.text = validateAndSanitizeText(newText);
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Borrado lógico: Archiva el comentario.
     */
    public void archive() {
        this.status = Status.ARCHIVED;
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Restaura el comentario al estado publicado.
     */
    public void restore() {
        this.status = Status.PUBLISHED;
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Pone el comentario en estado bajo revisión debido a reportes de moderación.
     */
    public void putUnderReview() {
        this.status = Status.UNDER_REVIEW;
        this.updatedAt = BogotaTime.now();
    }

    public boolean isArchived() {
        return this.status == Status.ARCHIVED;
    }

    public boolean isPublished() {
        return this.status == Status.PUBLISHED;
    }

    /**
     * Compatibilidad: Devuelve el {@link ContentId} si el comentario apunta a un contenido.
     */
    public ContentId getContentId() {
        if (this.targetType == CommentTargetType.CONTENT && this.targetId != null) {
            return ContentId.of(this.targetId);
        }
        return null;
    }

    private static String validateTargetId(String targetId) {
        if (targetId == null || targetId.isBlank()) {
            throw new DomainException("El identificador de destino es obligatorio");
        }
        return targetId.strip();
    }

    /**
     * Valida y sanitiza el texto ingresado.
     */
    private static String validateAndSanitizeText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new DomainException("El texto del comentario no puede estar vacío");
        }
        String sanitized = rawText.strip();
        if (sanitized.length() > MAX_TEXT_LENGTH) {
            throw new DomainException("El texto del comentario no puede exceder los " + MAX_TEXT_LENGTH + " caracteres");
        }
        return sanitized;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public CommentId getId() { return id; }
    public CommentTargetType getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public AccountId getAuthorId() { return authorId; }
    public String getText() { return text; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
