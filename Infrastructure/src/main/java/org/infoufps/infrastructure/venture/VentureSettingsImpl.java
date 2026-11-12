package org.infoufps.infrastructure.venture;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infoufps.application.venture.VentureSettings;

/**
 * Implementación de la configuración de La Vitrina mediante MicroProfile Config.
 */
@ApplicationScoped
public class VentureSettingsImpl implements VentureSettings {

    @ConfigProperty(name = "venture.max-per-user", defaultValue = "3")
    int maxVenturesPerUser;

    @ConfigProperty(name = "venture.daily-creation-limit", defaultValue = "5")
    int dailyCreationLimit;

    @ConfigProperty(name = "venture.report-threshold-review", defaultValue = "3")
    int reportThresholdForReview;

    @ConfigProperty(name = "venture.frontend.base-url", defaultValue = "http://localhost:5173")
    String frontendBaseUrl;

    @ConfigProperty(name = "venture.frontend.venture-path", defaultValue = "/emprendimientos/{id}")
    String frontendVenturePath;

    @ConfigProperty(name = "venture.api.base-url", defaultValue = "http://localhost:8080")
    String apiBaseUrl;

    @ConfigProperty(name = "venture.share.logo-url", defaultValue = "http://localhost:5173/logo.png")
    String shareLogoUrl;

    @Override
    public int maxVenturesPerUser() {
        return maxVenturesPerUser;
    }

    @Override
    public int dailyCreationLimit() {
        return dailyCreationLimit;
    }

    @Override
    public int reportThresholdForReview() {
        return reportThresholdForReview;
    }

    @Override
    public String frontendBaseUrl() {
        return frontendBaseUrl;
    }

    @Override
    public String frontendVenturePath() {
        return frontendVenturePath;
    }

    @Override
    public String apiBaseUrl() {
        return apiBaseUrl;
    }

    @Override
    public String shareLogoUrl() {
        return shareLogoUrl;
    }
}

