package org.infoufps.infrastructure.forum.persistence;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.forum.*;
import org.infoufps.domain.shared.time.BogotaTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de mapeo de repositorios JPA de Foro Comunitario")
class ForumRepositoryJpaTest {

    private final ForumThreadRepositoryJpa threadRepository = new ForumThreadRepositoryJpa();
    private final ForumLikeRepositoryJpa likeRepository = new ForumLikeRepositoryJpa();
    private final ForumReportRepositoryJpa reportRepository = new ForumReportRepositoryJpa();

    @Test
    @DisplayName("ForumThreadRepositoryJpa: mapeo bidireccional toEntity y toDomain")
    void forumThreadBidirectionalMapping() {
        AccountId authorId = AccountId.generate();
        ForumThread thread = ForumThread.create(
                "¿Cómo preparar el examen final de Algoritmos?",
                "Compartamos dudas y ejercicios resueltos para el examen de la próxima semana.",
                authorId
        );
        thread.update("¿Cómo preparar el examen final de Algoritmos? [Actualizado]",
                "Compartamos dudas y ejercicios resueltos. Agrego enlaces a problemas clave.");

        ForumThreadJpa entity = threadRepository.toEntity(thread);
        assertEquals(thread.getId().value(), entity.getThreadId());
        assertEquals(thread.getTitle(), entity.getTitle());
        assertEquals(thread.getBody(), entity.getBody());
        assertEquals(thread.getAuthorId().value(), entity.getAuthorId());
        assertEquals(Status.PUBLISHED, entity.getStatus());
        assertEquals(thread.getCreatedAt(), entity.getCreatedAt());
        assertEquals(thread.getUpdatedAt(), entity.getUpdatedAt());

        ForumThread domain = threadRepository.toDomain(entity);
        assertEquals(thread.getId(), domain.getId());
        assertEquals(thread.getTitle(), domain.getTitle());
        assertEquals(thread.getBody(), domain.getBody());
        assertEquals(thread.getAuthorId(), domain.getAuthorId());
        assertEquals(thread.getStatus(), domain.getStatus());
        assertEquals(thread.getCreatedAt(), domain.getCreatedAt());
        assertEquals(thread.getUpdatedAt(), domain.getUpdatedAt());
        assertTrue(domain.isEdited());
    }

    @Test
    @DisplayName("ForumLikeRepositoryJpa: mapeo bidireccional toEntity y toDomain")
    void forumLikeBidirectionalMapping() {
        AccountId accountId = AccountId.generate();
        ThreadId threadId = ThreadId.generate();
        ForumLike like = ForumLike.create(accountId, threadId);

        ForumLikeJpa entity = likeRepository.toEntity(like);
        assertEquals(accountId.value(), entity.getId().getAccountId());
        assertEquals(threadId.value(), entity.getId().getThreadId());
        assertEquals(like.getCreatedAt(), entity.getCreatedAt());

        ForumLike domain = likeRepository.toDomain(entity);
        assertEquals(accountId, domain.getAccountId());
        assertEquals(threadId, domain.getThreadId());
        assertEquals(like.getCreatedAt(), domain.getCreatedAt());
    }

    @Test
    @DisplayName("ForumReportRepositoryJpa: mapeo bidireccional toEntity y toDomain")
    void forumReportBidirectionalMapping() {
        AccountId reporterId = AccountId.generate();
        String threadTargetId = ThreadId.generate().value();

        ForumReport report = ForumReport.create(
                reporterId,
                ReportTargetType.THREAD,
                threadTargetId,
                ReportReason.SPAM,
                "Publicidad no autorizada de servicios externos"
        );
        report.resolve();

        ForumReportJpa entity = reportRepository.toEntity(report);
        assertEquals(report.getId().value(), entity.getReportId());
        assertEquals(reporterId.value(), entity.getReporterId());
        assertEquals(ReportTargetType.THREAD, entity.getTargetType());
        assertEquals(threadTargetId, entity.getTargetId());
        assertEquals(ReportReason.SPAM, entity.getReason());
        assertEquals("Publicidad no autorizada de servicios externos", entity.getComment());
        assertEquals(ReportStatus.RESOLVED, entity.getStatus());
        assertEquals(report.getCreatedAt(), entity.getCreatedAt());
        assertNotNull(entity.getResolvedAt());

        ForumReport domain = reportRepository.toDomain(entity);
        assertEquals(report.getId(), domain.getId());
        assertEquals(reporterId, domain.getReporterId());
        assertEquals(ReportTargetType.THREAD, domain.getTargetType());
        assertEquals(threadTargetId, domain.getTargetId());
        assertEquals(ReportReason.SPAM, domain.getReason());
        assertEquals(report.getComment(), domain.getComment());
        assertEquals(ReportStatus.RESOLVED, domain.getStatus());
        assertEquals(report.getCreatedAt(), domain.getCreatedAt());
        assertEquals(report.getResolvedAt(), domain.getResolvedAt());
    }
}

