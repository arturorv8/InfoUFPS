package org.infoufps.application.venture;

import org.infoufps.domain.venture.ContactType;
import org.infoufps.domain.venture.ServiceMode;
import org.infoufps.domain.venture.VentureCategory;

import java.util.Set;

/**
 * Comando para actualizar un emprendimiento existente.
 */
public record UpdateVentureCommand(
        String id,
        String name,
        String description,
        VentureCategory category,
        ContactType contactType,
        String contactValue,
        Set<ServiceMode> serviceModes,
        String location,
        String requesterId,
        boolean isAdmin
) {
}

