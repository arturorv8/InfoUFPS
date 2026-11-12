package org.infoufps.domain.academic;

import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoMateria;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.account.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MateriaDomainTest {

    @Test
    @DisplayName("Propuesta de materia: solo nombre es obligatorio; código en mayúsculas")
    void testProposeMateria() {
        AccountId proponente = AccountId.generate();
        Materia m = Materia.propose("Cálculo Diferencial", "  1150101  ", 4, TipoMateria.CARRERA, proponente);

        assertEquals("Cálculo Diferencial", m.getNombre());
        assertEquals("calculo diferencial", m.getNombreNormalizado());
        assertEquals("1150101", m.getCodigo());
        assertEquals(4, m.getCreditos());
        assertEquals(TipoMateria.CARRERA, m.getTipo());
        assertEquals(EstadoModeracion.EN_REVISION, m.getEstado());
        assertEquals(proponente, m.getProponenteId());

        // Propuesta con solo nombre es válida (reglas 3 y 9)
        Materia soloNombre = Materia.propose("Materia Opcional", null, null, null, proponente);
        assertEquals("Materia Opcional", soloNombre.getNombre());
        assertNull(soloNombre.getCodigo());
        assertNull(soloNombre.getCreditos());
        assertNull(soloNombre.getTipo());
    }

    @Test
    @DisplayName("Créditos deben ser enteros positivos")
    void testCreditosValidation() {
        AccountId proponente = AccountId.generate();
        assertThrows(DomainException.class, () ->
                Materia.propose("Materia Inválida", null, 0, null, proponente));
        assertThrows(DomainException.class, () ->
                Materia.propose("Materia Inválida", null, -3, null, proponente));
    }

    @Test
    @DisplayName("Ciclo de vida y transiciones de materia")
    void testMateriaLifecycle() {
        Materia m = Materia.propose("Física Mecánica", "1150201", 3, TipoMateria.CARRERA, AccountId.generate());
        assertEquals(EstadoModeracion.EN_REVISION, m.getEstado());

        m.publish();
        assertEquals(EstadoModeracion.PUBLICADO, m.getEstado());

        m.archive();
        assertEquals(EstadoModeracion.ARCHIVADO, m.getEstado());

        assertThrows(DomainException.class, m::publish);
    }

    @Test
    @DisplayName("Solo el proponente puede editar su propuesta de materia")
    void testOnlyProponentCanEditProposal() {
        AccountId proponente = AccountId.generate();
        AccountId intruso = AccountId.generate();
        Materia m = Materia.propose("Física Mecánica", "1150201", 3, TipoMateria.CARRERA, proponente);

        assertThrows(ForbiddenException.class, () ->
                m.updateProposal("Otro Nombre", "1150202", 4, TipoMateria.CARRERA, intruso));

        m.updateProposal("Física Mecánica II", "1150202", 4, TipoMateria.CARRERA, proponente);
        assertEquals("Física Mecánica II", m.getNombre());
    }
}

