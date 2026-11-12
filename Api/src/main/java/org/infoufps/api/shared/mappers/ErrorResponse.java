package org.infoufps.api.shared.mappers;

import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;

public record ErrorResponse(
        String error,
        String message,
        int status,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String error, String message, int status) {
        return new ErrorResponse(error, message, status, BogotaTime.now());
    }
}
