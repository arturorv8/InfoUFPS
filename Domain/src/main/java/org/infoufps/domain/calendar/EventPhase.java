package org.infoufps.domain.calendar;

/**
 * Fase calculada de un evento respecto de la fecha actual.
 * No se persiste; se deriva al consultar.
 */
public enum EventPhase {

    /** El evento aún no ha comenzado. */
    UPCOMING,

    /** El evento está ocurriendo; incluye el último día completo. */
    ONGOING,

    /** El evento ya finalizó. */
    FINISHED
}
