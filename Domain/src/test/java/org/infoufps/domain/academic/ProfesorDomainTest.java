package org.infoufps.domain.academic;

import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.MateriaId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfesorDomainTest {

    @Test
    @DisplayName("Propuesta de profesor inicia en EN_REVISION y normaliza nombre y email en minúsculas")
    void testProposeProfesor() {
        AccountId proponente = AccountId.generate();
        Profesor p = Profesor.propose("  Óscar Gómez  ", Facultad.INGENIERIA, "Oscar.Gomez@UFPS.edu.co", proponente);

        assertEquals("Óscar Gómez", p.getNombre());
        assertEquals("gomez oscar", p.getNombreNormalizado());
        assertEquals(Facultad.INGENIERIA, p.getFacultad());
        assertEquals("oscar.gomez@ufps.edu.co", p.getEmail());
        assertEquals(EstadoModeracion.EN_REVISION, p.getEstado());
        assertEquals(proponente, p.getProponenteId());
    }

    @Test
    @DisplayName("Admin crea profesor directamente en PUBLICADO")
    void testCreateByAdmin() {
        Profesor p = Profesor.createByAdmin("María López", Facultad.CIENCIAS_BASICAS, "maria@ufps.edu.co");
        assertEquals(EstadoModeracion.PUBLICADO, p.getEstado());
        assertNull(p.getProponenteId());
    }

    @Test
    @DisplayName("Transiciones de ciclo de vida válidas e inválidas para Profesor")
    void testLifecycleTransitions() {
        Profesor p = Profesor.propose("Carlos Silva", null, null, AccountId.generate());
        assertEquals(Facultad.NO_ESPECIFICADA, p.getFacultad(), "Si es nula se asigna NO_ESPECIFICADA por defecto");
        assertEquals(EstadoModeracion.EN_REVISION, p.getEstado());

        p.publish();
        assertEquals(EstadoModeracion.PUBLICADO, p.getEstado());

        p.archive();
        assertEquals(EstadoModeracion.ARCHIVADO, p.getEstado());

        // Intentar publicar desde archivado debe fallar (estado terminal)
        assertThrows(DomainException.class, p::publish);
    }

    @Test
    @DisplayName("Solo el proponente puede editar su propuesta y solo mientras esté en revisión")
    void testProposalEditionPermissions() {
        AccountId autor = AccountId.generate();
        AccountId intruso = AccountId.generate();

        Profesor p = Profesor.propose("Profesor Original", Facultad.CIENCIAS_DE_LA_SALUD, null, autor);

        // Intruso no puede editar
        assertThrows(ForbiddenException.class, () ->
                p.updateProposal("Nuevo Nombre", Facultad.INGENIERIA, null, intruso)
        );

        // Autor sí puede editar
        p.updateProposal("Nombre Editado", Facultad.INGENIERIA, "editado@ufps.edu.co", autor);
        assertEquals("Nombre Editado", p.getNombre());
        assertEquals("editado@ufps.edu.co", p.getEmail());

        // Una vez publicado, ya no se puede editar como propuesta
        p.publish();
        assertThrows(DomainException.class, () ->
                p.updateProposal("Otro Nombre", Facultad.INGENIERIA, null, autor)
        );
    }

    @Test
    @DisplayName("Validación de nombre de profesor (mínimo 2 caracteres, máximo 150)")
    void testNombreValidation() {
        AccountId autor = AccountId.generate();
        assertThrows(DomainException.class, () -> Profesor.propose("", Facultad.INGENIERIA, null, autor));
        assertThrows(DomainException.class, () -> Profesor.propose("a", Facultad.INGENIERIA, null, autor));
        assertThrows(DomainException.class, () -> Profesor.propose("a".repeat(151), Facultad.INGENIERIA, null, autor));
    }

    @Test
    @DisplayName("Asociación y remoción de materias")
    void testMateriasAssignment() {
        Profesor p = Profesor.createByAdmin("Profesor X", Facultad.INGENIERIA, null);
        MateriaId m1 = MateriaId.generate();
        MateriaId m2 = MateriaId.generate();

        p.assignMateria(m1);
        p.assignMateria(m2);
        assertEquals(2, p.getMaterias().size());

        p.removeMateria(m1);
        assertEquals(1, p.getMaterias().size());
        assertTrue(p.getMaterias().contains(m2));
    }
}

