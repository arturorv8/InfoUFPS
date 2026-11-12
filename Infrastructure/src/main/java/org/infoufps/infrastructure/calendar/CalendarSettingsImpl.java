package org.infoufps.infrastructure.calendar;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infoufps.application.calendar.CalendarSettings;

/**
 * Implementación de la configuración del calendario usando MicroProfile Config.
 */
@ApplicationScoped
public class CalendarSettingsImpl implements CalendarSettings {

    @ConfigProperty(name = "calendar.daily-proposal-limit", defaultValue = "5")
    int dailyProposalLimit;

    @ConfigProperty(name = "calendar.frontend.base-url", defaultValue = "http://localhost:5173")
    String frontendBaseUrl;

    @ConfigProperty(name = "calendar.frontend.event-path", defaultValue = "/eventos/{id}")
    String frontendEventPath;

    @ConfigProperty(name = "calendar.api.base-url", defaultValue = "http://localhost:8080")
    String apiBaseUrl;

    @ConfigProperty(name = "calendar.share.logo-url", defaultValue = "http://localhost:5173/logo.png")
    String shareLogoUrl;

    @Override
    public int dailyProposalLimit() {
        return dailyProposalLimit;
    }

    @Override
    public String frontendBaseUrl() {
        return frontendBaseUrl;
    }

    @Override
    public String frontendEventPath() {
        return frontendEventPath;
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
