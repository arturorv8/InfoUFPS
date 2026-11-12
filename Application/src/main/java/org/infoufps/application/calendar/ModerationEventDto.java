package org.infoufps.application.calendar;

import java.util.List;

/**
 * DTO de un evento dentro de la cola de moderación.
 * Incluye los posibles duplicados detectados por título, fecha y hora.
 *
 * @param event               datos del evento.
 * @param possibleDuplicates  eventos que podrían ser duplicados.
 */
public record ModerationEventDto(
        EventDto event,
        List<EventDto> possibleDuplicates
) {
    public ModerationEventDto {
        possibleDuplicates = possibleDuplicates == null ? List.of() : List.copyOf(possibleDuplicates);
    }

    /**
     * @return true si se detectaron posibles duplicados.
     */
    public boolean hasPossibleDuplicates() {
        return !possibleDuplicates.isEmpty();
    }
}
