package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.shared.exceptions.DailyLimitExceededException;
import org.infoufps.application.academic.AcademicSettings;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.account.AccountId;

@ApplicationScoped
public class DailyLimitService {

    public static final int DEFAULT_MAX_DAILY_PROFESOR_PROPOSALS = 10;
    public static final int DEFAULT_MAX_DAILY_MATERIA_PROPOSALS = 10;
    public static final int DEFAULT_MAX_DAILY_RESENAS = 15;

    public static final int MAX_DAILY_PROFESOR_PROPOSALS = DEFAULT_MAX_DAILY_PROFESOR_PROPOSALS;
    public static final int MAX_DAILY_MATERIA_PROPOSALS = DEFAULT_MAX_DAILY_MATERIA_PROPOSALS;
    public static final int MAX_DAILY_RESENAS = DEFAULT_MAX_DAILY_RESENAS;

    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;
    private final IResenaRepository resenaRepository;
    private final AcademicSettings settings;

    public DailyLimitService(IProfesorRepository profesorRepository,
                             IMateriaRepository materiaRepository,
                             IResenaRepository resenaRepository) {
        this(profesorRepository, materiaRepository, resenaRepository, null);
    }

    @Inject
    public DailyLimitService(IProfesorRepository profesorRepository,
                             IMateriaRepository materiaRepository,
                             IResenaRepository resenaRepository,
                             AcademicSettings settings) {
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
        this.resenaRepository = resenaRepository;
        this.settings = settings;
    }

    public int getMaxDailyProfesorProposals() {
        return settings != null ? settings.dailyProfesorLimit() : DEFAULT_MAX_DAILY_PROFESOR_PROPOSALS;
    }

    public int getMaxDailyMateriaProposals() {
        return settings != null ? settings.dailyMateriaLimit() : DEFAULT_MAX_DAILY_MATERIA_PROPOSALS;
    }

    public int getMaxDailyResenas() {
        return settings != null ? settings.dailyResenaLimit() : DEFAULT_MAX_DAILY_RESENAS;
    }

    public void checkProfesorProposalLimit(AccountId accountId) {
        if (profesorRepository != null) {
            int max = getMaxDailyProfesorProposals();
            long count = profesorRepository.countTodaySubmissionsByProponente(accountId);
            if (count >= max) {
                throw new DailyLimitExceededException("Has alcanzado el límite diario de " + max + " propuestas de profesores");
            }
        }
    }

    public void checkMateriaProposalLimit(AccountId accountId) {
        if (materiaRepository != null) {
            int max = getMaxDailyMateriaProposals();
            long count = materiaRepository.countTodaySubmissionsByProponente(accountId);
            if (count >= max) {
                throw new DailyLimitExceededException("Has alcanzado el límite diario de " + max + " propuestas de materias");
            }
        }
    }

    public void checkResenaLimit(AccountId accountId) {
        if (resenaRepository != null) {
            int max = getMaxDailyResenas();
            long count = resenaRepository.countTodaySubmissionsByAutor(accountId);
            if (count >= max) {
                throw new DailyLimitExceededException("Has alcanzado el límite diario de " + max + " reseñas");
            }
        }
    }
}
