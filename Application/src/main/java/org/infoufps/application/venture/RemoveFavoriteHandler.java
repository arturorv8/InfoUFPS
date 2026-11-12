package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.venture.IFavoriteVentureRepository;
import org.infoufps.domain.venture.VentureId;

/**
 * Caso de uso: Quitar un emprendimiento de favoritos.
 */
@ApplicationScoped
public class RemoveFavoriteHandler {

    private final IFavoriteVentureRepository favoriteRepository;

    @Inject
    public RemoveFavoriteHandler(IFavoriteVentureRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public void execute(RemoveFavoriteCommand command) {
        favoriteRepository.delete(
                AccountId.of(command.accountId()),
                VentureId.of(command.ventureId())
        );
    }
}

