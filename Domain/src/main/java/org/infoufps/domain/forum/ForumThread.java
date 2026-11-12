package org.infoufps.domain.forum;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de Dominio que representa un hilo de discusión en el foro comunitario.
 * <p>
 * Reglas de negocio del modelo:
 * - El título es obligatorio y tiene una longitud máxima de 150 caracteres.
 * - El cuerpo es obligatorio y tiene una longitud máxima de 5000 caracteres.
 * - Pertenece a un autor (cuenta) específico.
 * - Publicación inmediata: los hilos nacen con estado {@link Status#PUBLISHED}.
 * - Se considera "editado" si {@code updatedAt} es posterior a {@code createdAt}.
 * - No incluye categoría, fijado, cerrado ni última actividad.
 * - Borrado lógico mediante transición al estado {@link Status#ARCHIVED}.
 */
public class ForumThread {

    public static final int MAX_TITLE_LENGTH = 150;
    public static final int MAX_BODY_LENGTH = 5000;

    private final ThreadId id;
    private String title;
    private String body;
    private final AccountId authorId;
    private Status status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ForumThread(ThreadId id,
                        String title,
                        String body,
                        AccountId authorId,
                        Status status,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "El id del hilo es obligatorio");
        this.title = validateAndSanitizeTitle(title);
        this.body = validateAndSanitizeBody(body);
        this.authorId = Objects.requireNonNull(authorId, "El autor del hilo es obligatorio");
        this.status = status != null ? status : Status.PUBLISHED;
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        this.updatedAt = updatedAt;
    }

    /**
     * Fábrica para crear un nuevo hilo de discusión.
     * Nace en estado {@link Status#PUBLISHED} y con marca de tiempo actual.
     */
    public static ForumThread create(String title, String body, AccountId authorId) {
        return new ForumThread(
                ThreadId.generate(),
                title,
                body,
                authorId,
                Status.PUBLISHED,
                BogotaTime.now(),
                null
        );
    }

    /**
     * Fábrica para reconstruir un hilo desde la capa de persistencia.
     */
    public static ForumThread of(ThreadId id,
                                 String title,
                                 String body,
                                 AccountId authorId,
                                 Status status,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt) {
        return new ForumThread(id, title, body, authorId, status, createdAt, updatedAt);
    }

    /**
     * Actualiza el contenido del hilo (título y cuerpo).
     * Registra la marca de tiempo de modificación, considerándose a partir de entonces como "editado".
     */
    public void update(String newTitle, String newBody) {
        this.title = validateAndSanitizeTitle(newTitle);
        this.body = validateAndSanitizeBody(newBody);
        LocalDateTime now = BogotaTime.now();
        this.updatedAt = now.isAfter(this.createdAt) ? now : this.createdAt.plusNanos(1_000_000);
    }

    /**
     * Borrado lógico: Archiva el hilo impidiendo su visualización en el listado público.
     */
    public void archive() {
        this.status = Status.ARCHIVED;
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Restaura el hilo devolviéndolo al estado público.
     */
    public void restore() {
        this.status = Status.PUBLISHED;
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Pone el hilo en revisión preventiva tras superar el umbral de reportes.
     */
    public void putUnderReview() {
        this.status = Status.UNDER_REVIEW;
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Determina si el hilo ha sido modificado con posterioridad a su fecha de creación.
     */
    public boolean isEdited() {
        return updatedAt != null && updatedAt.isAfter(createdAt);
    }

    public boolean isPublished() {
        return this.status == Status.PUBLISHED;
    }

    public boolean isArchived() {
        return this.status == Status.ARCHIVED;
    }

    public boolean isUnderReview() {
        return this.status == Status.UNDER_REVIEW;
    }

    private static String validateAndSanitizeTitle(String rawTitle) {
        if (rawTitle == null || rawTitle.isBlank()) {
            throw new DomainException("El título del hilo no puede estar vacío");
        }
        String clean = rawTitle.strip();
        if (clean.length() > MAX_TITLE_LENGTH) {
            throw new DomainException("El título del hilo no puede exceder los " + MAX_TITLE_LENGTH + " caracteres");
        }
        return clean;
    }

    private static String validateAndSanitizeBody(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) {
            throw new DomainException("El cuerpo del hilo no puede estar vacío");
        }
        String clean = rawBody.strip();
        if (clean.length() > MAX_BODY_LENGTH) {
            throw new DomainException("El cuerpo del hilo no puede exceder los " + MAX_BODY_LENGTH + " caracteres");
        }
        return clean;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public ThreadId getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public AccountId getAuthorId() { return authorId; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
