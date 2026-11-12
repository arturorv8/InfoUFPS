package org.infoufps.application.calendar;

import org.infoufps.application.calendar.CalendarSettings;

/**
 * Configuración fija del calendario para pruebas.
 */
public record FixedCalendarSettings(
        int dailyProposalLimit,
        String frontendBaseUrl,
        String frontendEventPath,
        String apiBaseUrl,
        String shareLogoUrl
) implements CalendarSettings {

    public static FixedCalendarSettings defaults() {
        return new FixedCalendarSettings(5,
                "https://info.ufps.edu.co",
                "/eventos/{id}",
                "https://api.ufps.edu.co",
                "https://info.ufps.edu.co/logo.png");
    }
}
