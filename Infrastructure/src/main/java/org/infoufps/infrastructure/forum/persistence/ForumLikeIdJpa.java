package org.infoufps.infrastructure.forum.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave primaria compuesta para un 'me gusta' en un hilo del foro: cuenta + hilo.
 */
@Embeddable
public class ForumLikeIdJpa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "account_id", length = 36, nullable = false)
    private String accountId;

    @Column(name = "thread_id", length = 36, nullable = false)
    private String threadId;

    public ForumLikeIdJpa() {
    }

    public ForumLikeIdJpa(String accountId, String threadId) {
        this.accountId = accountId;
        this.threadId = threadId;
    }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForumLikeIdJpa that)) return false;
        return Objects.equals(accountId, that.accountId) && Objects.equals(threadId, that.threadId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, threadId);
    }
}

