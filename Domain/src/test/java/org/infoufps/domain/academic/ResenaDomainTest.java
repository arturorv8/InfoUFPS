package org.infoufps.domain.academic;

import org.infoufps.domain.academic.Resena;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoDestino;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.MateriaId;
import org.infoufps.domain.academic.ProfesorId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResenaDomainTest {

    @Test
    @DisplayName("Crear reseña para profesor valida calificación 1-5 y comentario no vacío")
    void testCreateResenaProfesor() {
        AccountId autor = AccountId.generate();
        ProfesorId profId = ProfesorId.generate();

        Resena r = Resena.createForProfesor(autor, profId, 5, "Excelente profesor", "Cálculo");
        assertEquals(TipoDestino.PROFESOR, r.getTipoDestino());
        assertEquals(5, r.getCalificacion());
        assertEquals("Excelente profesor", r.getComentario());
        assertEquals("Cálculo", r.getMateriaCursada());
        assertEquals(EstadoModeracion.EN_REVISION, r.getEstado());

        // Calificación inválida
        assertThrows(DomainException.class, () -> Resena.createForProfesor(autor, profId, 0, "Texto", null));
        assertThrows(DomainException.class, () -> Resena.createForProfesor(autor, profId, 6, "Texto", null));

        // Comentario vacío o excesivo
        assertThrows(DomainException.class, () -> Resena.createForProfesor(autor, profId, 4, "", null));
        assertThrows(DomainException.class, () -> Resena.createForProfesor(autor, profId, 4, "a".repeat(1001), null));
    }

    @Test
    @DisplayName("Crear reseña para materia")
    void testCreateResenaMateria() {
        AccountId autor = AccountId.generate();
        MateriaId matId = MateriaId.generate();

        Resena r = Resena.createForMateria(autor, matId, 4, "Materia interesante con buen contenido");
        assertEquals(TipoDestino.MATERIA, r.getTipoDestino());
        assertEquals(matId, r.getMateriaId());
        assertNull(r.getProfesorId());
    }

    @Test
    @DisplayName("Editar reseña publicada no requiere nueva moderación (Supuesto 9)")
    void testEditPublishedReviewKeepsPublished() {
        AccountId autor = AccountId.generate();
        ProfesorId profId = ProfesorId.generate();

        Resena r = Resena.createForProfesor(autor, profId, 3, "Regular", null);
        r.publish();
        assertEquals(EstadoModeracion.PUBLICADO, r.getEstado());

        // Autor edita comentario y calificación
        r.updateByAuthor(4, "Mejoró bastante con las asesorías", "Álgebra Lineal", autor);
        assertEquals(4, r.getCalificacion());
        assertEquals("Mejoró bastante con las asesorías", r.getComentario());
        assertEquals("Álgebra Lineal", r.getMateriaCursada());
        assertEquals(EstadoModeracion.PUBLICADO, r.getEstado(), "Sigue en PUBLICADO sin requerir nueva moderación");
    }

    @Test
    @DisplayName("Solo el autor puede editar su reseña")
    void testOnlyAuthorCanEditReview() {
        AccountId autor = AccountId.generate();
        AccountId otro = AccountId.generate();
        Resena r = Resena.createForMateria(autor, MateriaId.generate(), 5, "Buena materia");

        assertThrows(ForbiddenException.class, () -> r.updateByAuthor(2, "Mala", null, otro));
    }
}

