package org.infoufps.domain.shared;

import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.academic.TipoDestino;
import org.infoufps.domain.academic.TipoMateria;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnumsTest {

    @Test
    @DisplayName("Facultad rechaza valores desconocidos con DomainException")
    void facultadRejectsUnknownValue() {
        assertThrows(DomainException.class, () -> Facultad.fromString("Facultad Inexistente"));
    }

    @Test
    @DisplayName("TipoMateria rechaza valores desconocidos con DomainException")
    void tipoMateriaRejectsUnknownValue() {
        assertThrows(DomainException.class, () -> TipoMateria.fromString("otro"));
    }

    @Test
    @DisplayName("TipoDestino rechaza valores desconocidos con DomainException")
    void tipoDestinoRejectsUnknownValue() {
        assertThrows(DomainException.class, () -> TipoDestino.fromString("OTRO"));
    }

    @Test
    @DisplayName("EstadoModeracion rechaza valores desconocidos con DomainException")
    void estadoModeracionRejectsUnknownValue() {
        assertThrows(DomainException.class, () -> EstadoModeracion.fromString("BORRADOR"));
    }

    @Test
    @DisplayName("Los valores válidos siguen resolviéndose correctamente")
    void validValuesStillResolve() {
        assertEquals(Facultad.INGENIERIA, Facultad.fromString("Ingeniería"));
        assertEquals(TipoMateria.CURSO_DE_FORMACION, TipoMateria.fromString("curso de formación"));
        assertEquals(TipoDestino.PROFESOR, TipoDestino.fromString("profesor"));
        assertEquals(EstadoModeracion.PUBLICADO, EstadoModeracion.fromString("publicado"));
    }

    @Test
    @DisplayName("Los valores vacíos mantienen el comportamiento previo")
    void blankValuesKeepPreviousBehavior() {
        assertEquals(Facultad.NO_ESPECIFICADA, Facultad.fromString(" "));
        assertNull(TipoMateria.fromString(null));
        assertNull(TipoDestino.fromString(""));
        assertNull(EstadoModeracion.fromString(null));
    }
}
