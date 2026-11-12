package org.infoufps.domain.forum;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PagedResult;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Puerto de Dominio para la persistencia y consulta de hilos de discusión del foro comunitario.
 */
public interface IForumThreadRepository {

    /**
     * Guarda o actualiza un hilo en el almacenamiento.
     */
    ForumThread save(ForumThread thread);

    /**
     * Busca un hilo por su identificador único.
     */
    Optional<ForumThread> findById(ThreadId id);

    /**
     * Obtiene el listado público de hilos con estado {@link Status#PUBLISHED},
     * ordenados del más reciente al más antiguo (created_at DESC).
     */
    PagedResult<ForumThread> findPublished(PageRequest pageRequest);

    /**
     * Obtiene los hilos creados por un usuario específico ordenados cronológicamente descendente.
     */
    PagedResult<ForumThread> findByAuthor(AccountId authorId, PageRequest pageRequest);

    /**
     * Consulta administrativa paginada de hilos, con filtro opcional por estado.
     */
    PagedResult<ForumThread> findAdmin(Status status, PageRequest pageRequest);

    /**
     * Cuenta la cantidad de hilos creados por un usuario a partir de una fecha/hora dada.
     * Utilizado para validar el límite diario de creación de hilos.
     */
    long countCreatedByAuthorSince(AccountId authorId, LocalDateTime since);

    /**
     * Elimina físicamente un hilo del almacenamiento.
     */
    void deleteById(ThreadId id);

    /**
     * Verifica la existencia de un hilo por su identificador.
     */
    boolean existsById(ThreadId id);
}

