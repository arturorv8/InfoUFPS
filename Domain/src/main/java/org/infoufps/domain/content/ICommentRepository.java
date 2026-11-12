package org.infoufps.domain.content;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PagedResult;

/**
 * Puerto del repositorio para la gestión y persistencia de la entidad {@link Comment}.
 * Define el contrato que debe implementar cualquier mecanismo de persistencia (JPA, En Memoria, etc.).
 */
public interface ICommentRepository {

    /**
     * Guarda o actualiza un comentario en el almacenamiento.
     *
     * @param comment La entidad de dominio Comment a persistir.
     * @return El comentario persistido.
     */
    Comment save(Comment comment);

    /**
     * Busca un comentario por su identificador único.
     *
     * @param id El Value Object CommentId que identifica al comentario.
     * @return Un Optional con el Comment si fue encontrado, o vacío en caso contrario.
     */
    Optional<Comment> findById(CommentId id);

    /**
     * Obtiene todos los comentarios publicados asociados a un contenido específico,
     * ordenados cronológicamente por su fecha de creación (del más antiguo al más reciente).
     *
     * @param contentId El identificador del contenido.
     * @return Lista de comentarios publicados pertenecientes al contenido.
     */
    List<Comment> findByContentId(ContentId contentId);

    /**
     * Obtiene todos los comentarios publicados asociados a un destino específico,
     * ordenados del más antiguo al más reciente.
     *
     * @param targetType Tipo de destino polimórfico.
     * @param targetId   Identificador del destino.
     * @return Lista de comentarios publicados.
     */
    List<Comment> findPublishedByTarget(CommentTargetType targetType, String targetId);

    /**
     * Obtiene de forma paginada los comentarios publicados de un destino específico,
     * ordenados del más antiguo al más reciente.
     *
     * @param targetType  Tipo de destino polimórfico.
     * @param targetId    Identificador del destino.
     * @param pageRequest Parámetros de paginación.
     * @return Página de comentarios publicados.
     */
    PagedResult<Comment> findPublishedByTarget(CommentTargetType targetType, String targetId, PageRequest pageRequest);

    /**
     * Obtiene todos los comentarios realizados por una cuenta/usuario específico.
     *
     * @param authorId El identificador del autor.
     * @return Lista de comentarios realizados por el usuario.
     */
    List<Comment> findByAuthorId(AccountId authorId);

    /**
     * Obtiene los comentarios de un autor en un contenido específico.
     *
     * @param contentId El identificador del contenido.
     * @param authorId  El identificador del autor.
     * @return Lista de comentarios realizados por el autor en dicho contenido.
     */
    List<Comment> findByContentIdAndAuthorId(ContentId contentId, AccountId authorId);

    /**
     * Cuenta el total de comentarios publicados asociados a un contenido.
     *
     * @param contentId El identificador del contenido.
     * @return Número total de comentarios publicados registrados en el contenido.
     */
    long countByContentId(ContentId contentId);

    /**
     * Cuenta el total de comentarios publicados asociados a un destino específico.
     *
     * @param targetType Tipo de destino polimórfico.
     * @param targetId   Identificador del destino.
     * @return Total de comentarios publicados.
     */
    long countPublishedByTarget(CommentTargetType targetType, String targetId);

    /**
     * Cuenta la cantidad de comentarios registrados por un autor a partir de una fecha/hora dada.
     * Utilizado para el control antispam de tasa por minuto.
     *
     * @param authorId Identificador del autor.
     * @param since    Fecha y hora de inicio de la ventana de tiempo.
     * @return Número de comentarios emitidos por el autor desde ese instante.
     */
    long countByAuthorIdSince(AccountId authorId, LocalDateTime since);

    /**
     * Elimina físicamente un comentario de la base de datos a partir de su ID.
     *
     * @param id El CommentId del comentario a eliminar.
     */
    void deleteById(CommentId id);

    /**
     * Verifica la existencia de un comentario por su identificador único.
     *
     * @param id El CommentId a verificar.
     * @return true si el comentario existe, false de lo contrario.
     */
    boolean existsById(CommentId id);
}
