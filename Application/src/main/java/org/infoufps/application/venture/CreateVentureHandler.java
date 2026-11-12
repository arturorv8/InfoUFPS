package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.exceptions.ConflictException;
import org.infoufps.domain.shared.exceptions.DailyLimitExceededException;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.venture.*;

import java.time.LocalDateTime;

/**
 * Caso de uso: Registrar un nuevo emprendimiento en La Vitrina.
 */
@ApplicationScoped
public class CreateVentureHandler {

    private final IVentureRepository ventureRepository;
    private final VentureSettings settings;

    @Inject
    public CreateVentureHandler(IVentureRepository ventureRepository, VentureSettings settings) {
        this.ventureRepository = ventureRepository;
        this.settings = settings;
    }

    public VentureDetailDto execute(CreateVentureCommand command) {
        AccountId ownerId = AccountId.of(command.ownerId());

        // Límite por usuario: máximo 3 emprendimientos
        long totalVentures = ventureRepository.countByOwnerId(ownerId);
        if (totalVentures >= settings.maxVenturesPerUser()) {
            throw new DomainException("Has alcanzado el límite máximo de emprendimientos permitidos (" + settings.maxVenturesPerUser() + ")");
        }

        // Límite por día: máximo 5 creaciones diarias
        LocalDateTime startOfDay = BogotaTime.now().toLocalDate().atStartOfDay();
        long dailyCount = ventureRepository.countCreatedByOwnerIdSince(ownerId, startOfDay);
        if (dailyCount >= settings.dailyCreationLimit()) {
            throw new DailyLimitExceededException("Has alcanzado el límite de creaciones diarias de emprendimientos (" + settings.dailyCreationLimit() + ")");
        }

        // Duplicados: mismo dueño y mismo nombre normalizado da 409
        String nameSearch = TextNormalizer.normalize(command.name());
        if (ventureRepository.existsByOwnerIdAndNameSearch(ownerId, nameSearch, null)) {
            throw new ConflictException("Ya tienes un emprendimiento registrado con este nombre");
        }

        Contact contact = Contact.of(command.contactType(), command.contactValue());
        Venture venture = Venture.create(
                command.name(),
                command.description(),
                command.category(),
                ownerId,
                contact,
                command.serviceModes(),
                command.location()
        );

        ventureRepository.save(venture);
        return VentureDetailDto.from(venture, RatingSummary.empty(), null);
    }
}

