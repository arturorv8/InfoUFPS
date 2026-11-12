package org.infoufps.application.academic;

public record ModeracionWarningDto(
        String id,
        String nombre,
        String estado,
        double similitud,
        String mensaje
) {
}

