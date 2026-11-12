package org.infoufps.application.venture;

/**
 * Contrato de configuración para el módulo de emprendimientos (La Vitrina).
 */
public interface VentureSettings {

    /**
     * @return Número máximo de emprendimientos permitidos por usuario (por defecto 3).
     */
    int maxVenturesPerUser();

    /**
     * @return Límite máximo de creaciones de emprendimientos por usuario por día (por defecto 5).
     */
    int dailyCreationLimit();

    /**
     * @return Umbral de reportes pendientes que devuelve un emprendimiento a revisión (por defecto 3).
     */
    int reportThresholdForReview();

    /**
     * @return URL base de la aplicación frontend.
     */
    String frontendBaseUrl();

    /**
     * @return Ruta en el frontend del detalle de un emprendimiento (admite el marcador {id}).
     */
    String frontendVenturePath();

    /**
     * @return URL base de la API pública.
     */
    String apiBaseUrl();

    /**
     * @return URL del logo institucional fijo usado para la vista previa al compartir.
     */
    String shareLogoUrl();
}

