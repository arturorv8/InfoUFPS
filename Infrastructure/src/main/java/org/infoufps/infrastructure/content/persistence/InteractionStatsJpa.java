package org.infoufps.infrastructure.content.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.infoufps.domain.content.InteractionStats;

/**
 * Objeto embebible (Embeddable) de JPA para la persistencia de las métricas de interacción.
 * <p>
 * Mapea directamente los contadores de me gusta y comentarios como columnas
 * ('likes_count' y 'comments_count') dentro de la tabla principal 'contents'.
 * Permite la conversión fluida entre el modelo de persistencia y el objeto de dominio {@link InteractionStats}.
 */
@Embeddable
public class InteractionStatsJpa {

    /** Mapeo relacional para la columna que almacena el total de likes de la tarjeta. */
    @Column(name = "likes_count", nullable = false)
    private Integer likesCount;

    /** Mapeo relacional para la columna que almacena el total de comentarios de la tarjeta. */
    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount;

    /**
     * Constructor sin argumentos requerido por la especificación de JPA/Hibernate.
     * Inicializa los contadores en 0 por defecto para evitar valores nulos en la base de datos.
     */
    public InteractionStatsJpa() {
        this.likesCount = 0;
        this.commentsCount = 0;
    }

    /**
     * Constructor parametrizado para instanciar las métricas desde los valores del dominio o del mapeador.
     *
     * @param likesCount Cantidad de likes a persistir.
     * @param commentsCount Cantidad de comentarios a persistir.
     */
    public InteractionStatsJpa(Integer likesCount, Integer commentsCount) {
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
    }

    // ── Getters y Setters ────────────────────────────────────────────────

    /**
     * @return El total de likes persistido.
     */
    public Integer getLikesCount() {
        return likesCount;
    }

    /**
     * @param likesCount Nuevo total de likes a establecer.
     */
    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    /**
     * @return El total de comentarios persistido.
     */
    public Integer getCommentsCount() {
        return commentsCount;
    }

    /**
     * @param commentsCount Nuevo total de comentarios a establecer.
     */
    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

}
