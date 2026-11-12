package org.infoufps.application.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.EventSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Datos de entrada para crear o editar un evento.
 * Los valores se validan en la entidad de dominio y en el objeto de valor de programación.
 */
public record EventRequest(
        @NotBlank @Size(max = 150) String title,
        String type,
        @Size(max = 255) String location,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime,
        @Size(max = 4000) String description,
        @Size(max = 512) String infoUrl
) {

    /**
     * @return tipo de evento solicitado o GENERAL si no llega.
     */
    public EventType toEventType() {
        return EventType.fromOrDefault(type);
    }

    /**
     * @return programación validada.
     */
    public EventSchedule toSchedule() {
        return EventSchedule.of(startDate, endDate, startTime, endTime);
    }
}
