package org.infoufps.application.content;

import org.infoufps.domain.content.CommentTargetType;

/**
 * Puerto de aplicación para validar la existencia y estado publicado del destino de un comentario.
 * Desacopla la creación de comentarios de las entidades específicas de cada módulo (Content, ForumThread, etc.).
 */
public interface ICommentTargetValidator {

    /**
     * Tipo de destino soportado por este validador.
     *
     * @return {@link CommentTargetType} gestionado.
     */
    CommentTargetType targetType();

    /**
     * Valida que el recurso destino exista y se encuentre en estado {@code PUBLISHED}.
     *
     * @param targetId Identificador del destino.
     * @throws org.infoufps.domain.shared.exceptions.DomainException si el recurso no existe o no está publicado.
     */
    void validateTargetExistsAndPublished(String targetId);
}

