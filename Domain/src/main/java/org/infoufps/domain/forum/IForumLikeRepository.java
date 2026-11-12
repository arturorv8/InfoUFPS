package org.infoufps.domain.forum;

import org.infoufps.domain.account.AccountId;

import java.util.Set;

/**
 * Puerto de Dominio para la gestión de interacciones "Me gusta" sobre hilos del foro.
 */
public interface IForumLikeRepository {

    /**
     * Registra un "Me gusta" de forma idempotente.
     *
     * @param accountId Identificador de la cuenta.
     * @param threadId  Identificador del hilo.
     * @return {@code true} si se insertó un nuevo me gusta, {@code false} si ya existía.
     */
    boolean addLike(AccountId accountId, ThreadId threadId);

    /**
     * Elimina un "Me gusta" de forma idempotente.
     *
     * @param accountId Identificador de la cuenta.
     * @param threadId  Identificador del hilo.
     * @return {@code true} si se eliminó el registro, {@code false} si no existía.
     */
    boolean removeLike(AccountId accountId, ThreadId threadId);

    /**
     * Verifica si una cuenta ya dio me gusta a un hilo.
     */
    boolean existsByAccountAndThread(AccountId accountId, ThreadId threadId);

    /**
     * Cuenta el total de me gusta recibidos por un hilo específico.
     */
    long countByThreadId(ThreadId threadId);

    /**
     * Obtiene el conjunto de identificadores de hilos a los cuales el usuario les ha dado me gusta.
     */
    Set<String> findLikedThreadIdsByAccount(AccountId accountId);
}

