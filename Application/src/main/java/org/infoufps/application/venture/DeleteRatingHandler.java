package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.venture.IVentureRatingRepository;
import org.infoufps.domain.venture.VentureId;

/**
 * Caso de uso: Eliminar la calificación otorgada a un emprendimiento.
 */
@ApplicationScoped
public class DeleteRatingHandler {

    private final IVentureRatingRepository ratingRepository;

    @Inject
    public DeleteRatingHandler(IVentureRatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public void execute(DeleteRatingCommand command) {
        ratingRepository.delete(
                AccountId.of(command.accountId()),
                VentureId.of(command.ventureId())
        );
    }
}

