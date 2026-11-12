package org.infoufps.domain.forum;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de Dominio que representa la interacción de "Me gusta" sobre un hilo del foro.
 * <p>
 * Reglas de negocio del modelo:
 * - Es único por pareja (accountId, threadId).
 * - Aplica únicamente sobre hilos, no sobre comentarios.
 * - Un usuario no puede dar me gusta a su propio hilo.
 */
public class ForumLike {

    private final AccountId accountId;
    private final ThreadId threadId;
    private final LocalDateTime createdAt;

    private ForumLike(AccountId accountId, ThreadId threadId, LocalDateTime createdAt) {
        this.accountId = Objects.requireNonNull(accountId, "El accountId es obligatorio");
        this.threadId = Objects.requireNonNull(threadId, "El threadId es obligatorio");
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de registro es obligatoria");
    }

    public static ForumLike create(AccountId accountId, ThreadId threadId) {
        return new ForumLike(accountId, threadId, BogotaTime.now());
    }

    public static ForumLike of(AccountId accountId, ThreadId threadId, LocalDateTime createdAt) {
        return new ForumLike(accountId, threadId, createdAt);
    }

    public AccountId getAccountId() { return accountId; }
    public ThreadId getThreadId() { return threadId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForumLike forumLike)) return false;
        return Objects.equals(accountId, forumLike.accountId) && Objects.equals(threadId, forumLike.threadId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, threadId);
    }
}

