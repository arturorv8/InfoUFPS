package org.infoufps.domain.forum;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Dominio para el Módulo de Foro Comunitario")
class ForumDomainTest {

    @Test
    @DisplayName("ForumThread: creación válida nace PUBLISHED y no editado")
    void threadCreationValid() {
        AccountId author = AccountId.generate();
        ForumThread thread = ForumThread.create("Primer debate", "Cuerpo del debate", author);

        assertNotNull(thread.getId());
        assertEquals("Primer debate", thread.getTitle());
        assertEquals("Cuerpo del debate", thread.getBody());
        assertEquals(author, thread.getAuthorId());
        assertEquals(Status.PUBLISHED, thread.getStatus());
        assertTrue(thread.isPublished());
        assertFalse(thread.isEdited());
        assertNotNull(thread.getCreatedAt());
        assertNull(thread.getUpdatedAt());
    }

    @Test
    @DisplayName("ForumThread: validaciones de título y cuerpo")
    void threadValidationRules() {
        AccountId author = AccountId.generate();

        assertThrows(DomainException.class, () -> ForumThread.create(null, "Cuerpo", author));
        assertThrows(DomainException.class, () -> ForumThread.create("   ", "Cuerpo", author));
        assertThrows(DomainException.class, () -> ForumThread.create("a".repeat(151), "Cuerpo", author));

        assertThrows(DomainException.class, () -> ForumThread.create("Título", null, author));
        assertThrows(DomainException.class, () -> ForumThread.create("Título", "   ", author));
        assertThrows(DomainException.class, () -> ForumThread.create("Título", "a".repeat(5001), author));
    }

    @Test
    @DisplayName("ForumThread: edición actualiza título, cuerpo y marca isEdited")
    void threadUpdateSetsEdited() {
        AccountId author = AccountId.generate();
        ForumThread thread = ForumThread.create("Título inicial", "Cuerpo inicial", author);

        thread.update("Título nuevo", "Cuerpo nuevo");

        assertEquals("Título nuevo", thread.getTitle());
        assertEquals("Cuerpo nuevo", thread.getBody());
        assertNotNull(thread.getUpdatedAt());
        assertTrue(thread.isEdited());
    }

    @Test
    @DisplayName("ForumThread: transiciones de ciclo de vida (archivo, restauración y revisión)")
    void threadLifecycleTransitions() {
        AccountId author = AccountId.generate();
        ForumThread thread = ForumThread.create("Título", "Cuerpo", author);

        thread.putUnderReview();
        assertEquals(Status.UNDER_REVIEW, thread.getStatus());
        assertTrue(thread.isUnderReview());

        thread.restore();
        assertEquals(Status.PUBLISHED, thread.getStatus());
        assertTrue(thread.isPublished());

        thread.archive();
        assertEquals(Status.ARCHIVED, thread.getStatus());
        assertTrue(thread.isArchived());
    }

    @Test
    @DisplayName("ForumReport: creación y resolución")
    void reportLifecycle() {
        AccountId reporter = AccountId.generate();
        ForumReport report = ForumReport.create(
                reporter,
                ReportTargetType.THREAD,
                "thread-123",
                ReportReason.SPAM,
                "Publicidad excesiva"
        );

        assertEquals(ReportStatus.PENDING, report.getStatus());
        assertTrue(report.isPending());
        assertNull(report.getResolvedAt());

        report.resolve();
        assertEquals(ReportStatus.RESOLVED, report.getStatus());
        assertFalse(report.isPending());
        assertNotNull(report.getResolvedAt());
    }
}

