package org.infoufps.infrastructure.academic;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infoufps.application.academic.AcademicSettings;

@ApplicationScoped
public class AcademicSettingsImpl implements AcademicSettings {

    @ConfigProperty(name = "academic.daily-limit.profesor", defaultValue = "10")
    int dailyProfesorLimit;

    @ConfigProperty(name = "academic.daily-limit.materia", defaultValue = "10")
    int dailyMateriaLimit;

    @ConfigProperty(name = "academic.daily-limit.resena", defaultValue = "15")
    int dailyResenaLimit;

    @Override
    public int dailyProfesorLimit() {
        return dailyProfesorLimit;
    }

    @Override
    public int dailyMateriaLimit() {
        return dailyMateriaLimit;
    }

    @Override
    public int dailyResenaLimit() {
        return dailyResenaLimit;
    }
}
