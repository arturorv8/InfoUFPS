package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.EventPhase;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO de un evento del calendario.
 * Incluye los datos persistidos y los valores calculados fase y días restantes.
 */
public record EventDto(
        String eventId,
        String title,
        EventType type,
        String typeLabel,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime,
        boolean allDay,
        String description,
        String infoUrl,
        Status status,
        String proposedBy,
        String authorName,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int revision,
        EventPhase phase,
        long daysRemaining
) {

    /**
     * Construye el DTO calculando fase y días restantes respecto de hoy.
     *
     * @param event      evento de dominio.
     * @param authorName nombre visible del autor.
     * @return DTO del evento.
     */
    public static EventDto from(Event event, String authorName) {
        return from(event, authorName, BogotaTime.today());
    }

    /**
     * Construye el DTO con una fecha de referencia explícita.
     *
     * @param event         evento de dominio.
     * @param authorName    nombre visible del autor.
     * @param referenceDate fecha de referencia para fase y días restantes.
     * @return DTO del evento.
     */
    public static EventDto from(Event event, String authorName, LocalDate referenceDate) {
        return new EventDto(
                event.getId().value(),
                event.getTitle(),
                event.getType(),
                event.getType().getLabel(),
                event.getLocation(),
                event.getStartDate(),
                event.getEndDate(),
                event.getStartTime(),
                event.getEndTime(),
                event.isAllDay(),
                event.getDescription(),
                event.getInfoUrl(),
                event.getStatus(),
                event.getProposedBy().value(),
                authorName,
                event.getPublishedAt(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                event.getRevision(),
                event.phase(referenceDate),
                event.daysRemaining(referenceDate)
        );
    }
}
