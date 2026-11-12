package org.infoufps.application.calendar;

import java.time.LocalDate;

/**
 * DTO del resumen diario de eventos publicados.
 *
 * @param date  día.
 * @param count cantidad de eventos publicados que cubren el día.
 */
public record EventDaySummaryDto(LocalDate date, long count) {
}
