package org.infoufps.domain.calendar;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Objeto de Valor inmutable que encapsula la programación de un evento.
 * <p>
 * Reglas:
 * <ul>
 *   <li>La fecha de inicio es obligatoria.</li>
 *   <li>Si no se informa fecha de fin, se usa la fecha de inicio.</li>
 *   <li>La fecha de fin no puede ser anterior a la de inicio.</li>
 *   <li>Las horas son opcionales y representan un evento de todo el día cuando no llegan.</li>
 *   <li>Las horas solo aplican a eventos de un único día y deben venir ambas.</li>
 *   <li>La hora de fin debe ser estrictamente mayor que la de inicio.</li>
 * </ul>
 */
public final class EventSchedule {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalTime startTime;
    private final LocalTime endTime;

    private EventSchedule(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
        if (startDate == null) {
            throw new DomainException("La fecha de inicio es obligatoria");
        }

        LocalDate normalizedEnd = endDate != null ? endDate : startDate;
        if (normalizedEnd.isBefore(startDate)) {
            throw new DomainException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        if ((startTime == null) != (endTime == null)) {
            throw new DomainException("Las horas de inicio y fin deben informarse juntas");
        }

        if (startTime != null) {
            if (!startDate.equals(normalizedEnd)) {
                throw new DomainException("Las horas solo aplican a eventos de un único día");
            }
            if (!endTime.isAfter(startTime)) {
                throw new DomainException("La hora de fin debe ser posterior a la hora de inicio");
            }
        }

        this.startDate = startDate;
        this.endDate = normalizedEnd;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Crea una programación validada.
     *
     * @param startDate fecha de inicio obligatoria.
     * @param endDate   fecha de fin; si es nula se usa la fecha de inicio.
     * @param startTime hora de inicio opcional.
     * @param endTime   hora de fin opcional.
     * @return programación validada.
     */
    public static EventSchedule of(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
        return new EventSchedule(startDate, endDate, startTime, endTime);
    }

    /**
     * Alias de fábrica para creación desde la capa de aplicación.
     *
     * @param startDate fecha de inicio.
     * @param endDate   fecha de fin.
     * @param startTime hora de inicio.
     * @param endTime   hora de fin.
     * @return programación validada.
     */
    public static EventSchedule create(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
        return of(startDate, endDate, startTime, endTime);
    }

    /**
     * @return fecha de inicio.
     */
    public LocalDate startDate() {
        return startDate;
    }

    /**
     * @return fecha de fin (nunca nula).
     */
    public LocalDate endDate() {
        return endDate;
    }

    /**
     * @return hora de inicio o null si el evento es de todo el día.
     */
    public LocalTime startTime() {
        return startTime;
    }

    /**
     * @return hora de fin o null si el evento es de todo el día.
     */
    public LocalTime endTime() {
        return endTime;
    }

    /**
     * @return true si el evento no tiene horas y dura todo el día.
     */
    public boolean isAllDay() {
        return startTime == null;
    }

    /**
     * @return true si el evento abarca más de un día.
     */
    public boolean isMultiDay() {
        return !startDate.equals(endDate);
    }

    /**
     * Convierte el inicio local a UTC.
     *
     * @return inicio en UTC.
     */
    public ZonedDateTime startUtc() {
        return BogotaTime.toUtc(startDate, startTime != null ? startTime : LocalTime.MIDNIGHT);
    }

    /**
     * Convierte el fin local a UTC. Para eventos de todo el día usa el final del día.
     *
     * @return fin en UTC.
     */
    public ZonedDateTime endUtc() {
        if (startTime != null) {
            return BogotaTime.toUtc(endDate, endTime);
        }
        return BogotaTime.toUtc(endDate.plusDays(1), LocalTime.MIDNIGHT);
    }

    // Alias con prefijo get para facilitar el uso desde plantillas o mapeos.

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventSchedule that)) return false;
        return Objects.equals(startDate, that.startDate)
                && Objects.equals(endDate, that.endDate)
                && Objects.equals(startTime, that.startTime)
                && Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate, startTime, endTime);
    }

    @Override
    public String toString() {
        return "EventSchedule{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
