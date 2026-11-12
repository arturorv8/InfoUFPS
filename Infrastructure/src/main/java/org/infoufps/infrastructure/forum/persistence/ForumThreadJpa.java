package org.infoufps.infrastructure.forum.persistence;

import jakarta.persistence.*;
import org.infoufps.domain.calendar.Status;

import java.time.LocalDateTime;

/**
 * Entidad de Persistencia JPA para la tabla 'forum_threads'.
 */
@Entity
@Table(name = "forum_threads",
        indexes = {
                @Index(name = "idx_forum_threads_status_created", columnList = "status, created_at"),
                @Index(name = "idx_forum_threads_author_created", columnList = "author_id, created_at")
        })
public class ForumThreadJpa {

    @Id
    @Column(name = "thread_id", length = 36, nullable = false)
    private String threadId;

    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "author_id", length = 36, nullable = false)
    private String authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.PUBLISHED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public ForumThreadJpa() {
    }

    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

