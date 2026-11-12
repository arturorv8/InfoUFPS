package org.infoufps.domain.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.util.List;

/**
 * Puerto de Dominio para la gestión de emprendimientos favoritos.
 */
public interface IFavoriteVentureRepository {

    /**
     * Guarda la relación de favorito (idempotente).
     */
    FavoriteVenture save(FavoriteVenture favorite);

    /**
     * Comprueba si una cuenta tiene marcado un emprendimiento como favorito.
     */
    boolean existsByAccountIdAndVentureId(AccountId accountId, VentureId ventureId);

    /**
     * Elimina el favorito de una cuenta para un emprendimiento.
     */
    void delete(AccountId accountId, VentureId ventureId);

    /**
     * Obtiene todos los IDs de emprendimientos favoritos de una cuenta.
     */
    List<VentureId> findFavoriteVentureIdsByAccountId(AccountId accountId);

    /**
     * Lista de manera paginada los emprendimientos favoritos de una cuenta.
     * Solo retorna aquellos que actualmente están en estado PUBLISHED.
     */
    PageResult<Venture> findFavoriteVenturesByAccountId(AccountId accountId, PageRequest pageRequest);
}

