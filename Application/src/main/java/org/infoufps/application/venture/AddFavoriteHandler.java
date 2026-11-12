package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.venture.*;

/**
 * Caso de uso: Agregar un emprendimiento a favoritos (idempotente, solo publicados).
 */
@ApplicationScoped
public class AddFavoriteHandler {

    private final IVentureRepository ventureRepository;
    private final IFavoriteVentureRepository favoriteRepository;

    @Inject
    public AddFavoriteHandler(IVentureRepository ventureRepository, IFavoriteVentureRepository favoriteRepository) {
        this.ventureRepository = ventureRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public void execute(AddFavoriteCommand command) {
        Venture venture = ventureRepository.findById(VentureId.of(command.ventureId()))
                .orElseThrow(() -> new NotFoundException("Emprendimiento no encontrado: " + command.ventureId()));

        if (venture.getStatus() != Status.PUBLISHED) {
            throw new DomainException("Solo se pueden agregar a favoritos emprendimientos publicados");
        }

        AccountId accountId = AccountId.of(command.accountId());
        favoriteRepository.save(FavoriteVenture.create(accountId, venture.getId()));
    }
}

