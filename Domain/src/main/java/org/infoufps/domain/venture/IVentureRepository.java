package org.infoufps.domain.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de Dominio para la persistencia y consulta de emprendimientos.
 */
public interface IVentureRepository {

    /**
     * Guarda o actualiza un emprendimiento.
     */
    Venture save(Venture venture);

    /**
     * Busca un emprendimiento por su identificador.
     */
    Optional<Venture> findById(VentureId id);

    /**
     * Verifica si existe otro emprendimiento del mismo dueño con el mismo nombre normalizado.
     */
    boolean existsByOwnerIdAndNameSearch(AccountId ownerId, String nameSearch, VentureId excludeId);

    /**
     * Verifica si existe un emprendimiento con el mismo nombre normalizado perteneciente a otro dueño.
     */
    boolean existsByNameSearchAndDifferentOwner(String nameSearch, AccountId ownerId);

    /**
     * Verifica si existe un emprendimiento con el mismo valor de contacto perteneciente a otro dueño.
     */
    boolean existsByContactValueAndDifferentOwner(String contactValue, AccountId ownerId);

    /**
     * Cuenta el total de emprendimientos activos (o totales) asociados al dueño (límite de 3).
     */
    long countByOwnerId(AccountId ownerId);

    /**
     * Cuenta los emprendimientos creados por el dueño desde una fecha y hora específica (límite diario).
     */
    long countCreatedByOwnerIdSince(AccountId ownerId, LocalDateTime since);

    /**
     * Búsqueda y filtrado paginado de emprendimientos visibles para el público.
     */
    PageResult<Venture> search(VentureSearchCriteria criteria);

    /**
     * Obtiene todos los emprendimientos de un usuario.
     */
    List<Venture> findByOwnerId(AccountId ownerId);

    /**
     * Consulta paginada para administradores, con filtro opcional por estado.
     */
    PageResult<Venture> findAdmin(Status status, PageRequest pageRequest);

    /**
     * Elimina físicamente un emprendimiento y sus dependencias en cascada.
     */
    void delete(VentureId id);
}

