package org.infoufps.infrastructure.content.persistence;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.content.Comment;
import org.infoufps.domain.content.CommentId;
import org.infoufps.domain.content.CommentTargetType;
import org.infoufps.domain.shared.time.BogotaTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("CommentRepositoryJpa: mapeo toDomain y toEntity")
class CommentRepositoryJpaTest {

    private final CommentRepositoryJpa repository = new CommentRepositoryJpa();

    @Test
    @DisplayName("toEntity mapea correctamente todos los atributos genéricos")
    void toEntityMapsAllGenericFields() {
        Comment comment = Comment.create(
                CommentTargetType.CONTENT,
                "content-123",
                AccountId.generate(),
                "Comentario de prueba"
        );

        CommentJpa entity = repository.toEntity(comment);

        assertEquals(comment.getId().value(), entity.getCommentId());
        assertEquals(CommentTargetType.CONTENT, entity.getTargetType());
        assertEquals("content-123", entity.getTargetId());
        assertEquals(comment.getAuthorId().value(), entity.getAuthorId());
        assertEquals("Comentario de prueba", entity.getText());
        assertEquals(Status.PUBLISHED, entity.getStatus());
        assertNotNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("toDomain reconstruye la entidad de dominio con soporte de borrado lógico")
    void toDomainReconstructsArchivedComment() {
        LocalDateTime created = BogotaTime.now().minusHours(2);
        LocalDateTime updated = BogotaTime.now();

        CommentJpa jpa = new CommentJpa();
        jpa.setCommentId("c-999");
        jpa.setTargetType(CommentTargetType.FORUM_THREAD);
        jpa.setTargetId("thread-456");
        jpa.setAuthorId("author-789");
        jpa.setText("Texto moderado o archivado");
        jpa.setStatus(Status.ARCHIVED);
        jpa.setCreatedAt(created);
        jpa.setUpdatedAt(updated);

        Comment domain = repository.toDomain(jpa);

        assertEquals("c-999", domain.getId().value());
        assertEquals(CommentTargetType.FORUM_THREAD, domain.getTargetType());
        assertEquals("thread-456", domain.getTargetId());
        assertEquals("author-789", domain.getAuthorId().value());
        assertEquals("Texto moderado o archivado", domain.getText());
        assertEquals(Status.ARCHIVED, domain.getStatus());
        assertEquals(created, domain.getCreatedAt());
        assertEquals(updated, domain.getUpdatedAt());
    }
}

