package org.infoufps.infrastructure.content.persistence;

import jakarta.persistence.*;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.content.CommentTargetType;

import java.time.LocalDateTime;

/**
 * Entidad de Persistencia (JPA/Hibernate) que mapea el modelo relacional para la tabla 'comments'.
 * <p>
 * Representa la tabla polimórfica de comentarios genéricos en el sistema.
 * Soporta destinos tanto de contenidos como de hilos de foro, con borrado lógico (status = ARCHIVED).
 */
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_target_status_created", columnList = "target_type, target_id, status, created_at"),
        @Index(name = "idx_comments_author_created", columnList = "author_id, created_at")
})
public class CommentJpa {

    /** Identificador primario de la tabla (UUID de 36 caracteres en formato String). */
    @Id
    @Column(name = "comment_id", length = 36, nullable = false)
    private String commentId;

    /** Tipo de destino polimórfico (CONTENT o FORUM_THREAD). */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 50, nullable = false)
    private CommentTargetType targetType;

    /** Identificador de la entidad destino a la que pertenece el comentario. */
    @Column(name = "target_id", length = 36, nullable = false)
    private String targetId;

    /** Referencia al identificador de la cuenta o usuario que realizó el comentario. */
    @Column(name = "author_id", length = 36, nullable = false)
    private String authorId;

    /** Texto del comentario. */
    @Column(name = "text", columnDefinition = "TEXT", nullable = false)
    private String text;

    /** Estado del comentario (PUBLISHED, ARCHIVED, UNDER_REVIEW). */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status;

    /** Fecha y hora de creación del comentario. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Fecha y hora de la última modificación. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Constructor por defecto sin argumentos requerido por la especificación JPA/Hibernate.
     */
    public CommentJpa() {
    }

    // ── Getters y Setters ────────────────────────────────────────────────

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public CommentTargetType getTargetType() { return targetType; }
    public void setTargetType(CommentTargetType targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
