package org.infoufps.application.forum.dto;

import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;

/**
 * Proyección pública de la identidad del autor de un hilo o contenido.
 * Si la cuenta no existe o fue eliminada, refleja "Usuario eliminado".
 */
public record AuthorSummaryDto(
        String id,
        String name
) {
    public static AuthorSummaryDto from(AccountId authorId, Account account) {
        if (account == null) {
            return new AuthorSummaryDto(authorId != null ? authorId.value() : "desconocido", "Usuario eliminado");
        }
        return new AuthorSummaryDto(account.getAccountId().value(), account.getName());
    }

    public static AuthorSummaryDto deleted(String authorId) {
        return new AuthorSummaryDto(authorId, "Usuario eliminado");
    }
}

