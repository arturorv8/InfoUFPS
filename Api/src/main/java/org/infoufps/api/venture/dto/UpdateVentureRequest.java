package org.infoufps.api.venture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.infoufps.domain.venture.ContactType;
import org.infoufps.domain.venture.ServiceMode;
import org.infoufps.domain.venture.VentureCategory;

import java.util.Set;

/**
 * Petición para actualizar un emprendimiento existente en La Vitrina.
 */
public record UpdateVentureRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String name,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        String description,

        @NotNull(message = "La categoría es obligatoria")
        VentureCategory category,

        @NotNull(message = "El tipo de contacto es obligatorio")
        ContactType contactType,

        @NotBlank(message = "El valor de contacto es obligatorio")
        String contactValue,

        @NotEmpty(message = "Debe especificar al menos una modalidad de servicio")
        Set<ServiceMode> serviceModes,

        @Size(max = 255, message = "La ubicación no puede exceder 255 caracteres")
        String location
) {
}

