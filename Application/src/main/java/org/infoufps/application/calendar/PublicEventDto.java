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
 * DTO público de un evento del calendario.
 * No expone el identificador de la cuenta que propuso el evento ({@code proposedBy});
 * el nombre visible del autor sí es información pública.
 */
public record PublicEventDto(
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
        String authorName,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int revision,
        EventPhase phase,
        long daysRemaining
) {

    public static PublicEventDto from(Event event, String authorName) {
        return from(event, authorName, BogotaTime.today());
    }

    public static PublicEventDto from(Event event, String authorName, LocalDate referenceDate) {
        return new PublicEventDto(
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
