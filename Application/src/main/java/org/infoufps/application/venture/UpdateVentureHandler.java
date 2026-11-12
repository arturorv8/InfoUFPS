package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.exceptions.ConflictException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.venture.*;

/**
 * Caso de uso: Editar un emprendimiento existente.
 */
@ApplicationScoped
public class UpdateVentureHandler {

    private final IVentureRepository ventureRepository;
    private final IVentureRatingRepository ratingRepository;

    @Inject
    public UpdateVentureHandler(IVentureRepository ventureRepository, IVentureRatingRepository ratingRepository) {
        this.ventureRepository = ventureRepository;
        this.ratingRepository = ratingRepository;
    }

    public VentureDetailDto execute(UpdateVentureCommand command) {
        Venture venture = ventureRepository.findById(VentureId.of(command.id()))
                .orElseThrow(() -> new NotFoundException("Emprendimiento no encontrado: " + command.id()));

        // Duplicados: mismo dueño y mismo nombre normalizado da 409
        String nameSearch = TextNormalizer.normalize(command.name());
        if (ventureRepository.existsByOwnerIdAndNameSearch(venture.getOwnerId(), nameSearch, venture.getId())) {
            throw new ConflictException("Ya tienes un emprendimiento registrado con este nombre");
        }

        Contact newContact = Contact.of(command.contactType(), command.contactValue());
        venture.update(
                command.name(),
                command.description(),
                command.category(),
                newContact,
                command.serviceModes(),
                command.location(),
                AccountId.of(command.requesterId()),
                command.isAdmin()
        );

        ventureRepository.save(venture);
        RatingSummary ratingSummary = ratingRepository.getRatingSummary(venture.getId());
        return VentureDetailDto.from(venture, ratingSummary, null);
    }
}

