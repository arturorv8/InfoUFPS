package org.infoufps.application.venture;

/**
 * DTO para el endpoint de metadatos para compartir un emprendimiento.
 */
public record VentureShareDto(
        String id,
        String title,
        String description,
        String shareUrl,
        String logoUrl
) {
}

