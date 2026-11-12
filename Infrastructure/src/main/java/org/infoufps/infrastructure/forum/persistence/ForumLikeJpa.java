package org.infoufps.infrastructure.forum.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad de Persistencia JPA para la tabla 'forum_likes'.
 */
@Entity
@Table(name = "forum_likes",
        indexes = {
                @Index(name = "idx_forum_likes_thread_id", columnList = "thread_id"),
                @Index(name = "idx_forum_likes_account_id", columnList = "account_id")
        })
public class ForumLikeJpa {

    @EmbeddedId
    private ForumLikeIdJpa id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public ForumLikeJpa() {
    }

    public ForumLikeJpa(ForumLikeIdJpa id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public ForumLikeIdJpa getId() { return id; }
    public void setId(ForumLikeIdJpa id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

