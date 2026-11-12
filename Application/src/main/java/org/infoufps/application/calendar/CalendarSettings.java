package org.infoufps.application.calendar;

/**
 * Configuración del módulo de calendario.
 * La implementación vive en Infrastructure para mantener la capa de aplicación
 * libre de detalles de configuración de Quarkus.
 */
public interface CalendarSettings {

    /**
     * @return máximo de propuestas que un usuario puede crear por día.
     */
    int dailyProposalLimit();

    /**
     * @return URL base del frontend.
     */
    String frontendBaseUrl();

    /**
     * @return ruta del detalle de un evento en el frontend; admite el marcador {id}.
     */
    String frontendEventPath();

    /**
     * @return URL base pública de la API.
     */
    String apiBaseUrl();

    /**
     * @return URL del logo fijo usado en la vista previa al compartir.
     */
    String shareLogoUrl();
}
