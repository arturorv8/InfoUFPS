package org.infoufps.domain.shared.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Utilidad central de tiempo del sistema.
 * <p>
 * Todo el módulo de calendario trabaja con fechas y horas locales de la zona
 * {@code America/Bogota} (UTC-5, sin horario de verano). No se realiza ninguna
 * conversión por usuario. Las conversiones a UTC solo se usan al generar archivos
 * iCalendar o enlaces de Google Calendar.
 */
public final class BogotaTime {

    /** Zona horaria única de la aplicación. */
    public static final ZoneId ZONE = ZoneId.of("America/Bogota");

    private BogotaTime() {
    }

    /**
     * Fecha actual en la zona America/Bogota.
     *
     * @return fecha local actual.
     */
    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    /**
     * Fecha y hora actual en la zona America/Bogota.
     *
     * @return fecha-hora local actual.
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    /**
     * Construye un instante zonal en America/Bogota.
     *
     * @param date fecha local.
     * @param time hora local.
     * @return instante zonal en la zona de la aplicación.
     */
    public static ZonedDateTime at(LocalDate date, LocalTime time) {
        return ZonedDateTime.of(date, time, ZONE);
    }

    /**
     * Convierte una fecha y hora local de America/Bogota a UTC.
     *
     * @param date fecha local.
     * @param time hora local.
     * @return el mismo instante expresado en UTC.
     */
    public static ZonedDateTime toUtc(LocalDate date, LocalTime time) {
        return at(date, time).withZoneSameInstant(ZoneOffset.UTC);
    }

    /**
     * Fecha y hora actual en UTC, usada para marcas técnicas como DTSTAMP.
     *
     * @return fecha-hora UTC actual.
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
