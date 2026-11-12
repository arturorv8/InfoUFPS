package org.infoufps.domain.academic;

import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoDestino;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.MateriaId;
import org.infoufps.domain.academic.ProfesorId;
import org.infoufps.domain.academic.Rating;
import org.infoufps.domain.academic.ResenaId;

import java.time.LocalDateTime;
import java.util.Objects;

public class Resena {

    private final ResenaId id;
    private final AccountId autorId;
    private final TipoDestino tipoDestino;
    private final ProfesorId profesorId;
    private final MateriaId materiaId;
    private int calificacion;
    private String comentario;
    private String materiaCursada;
    private EstadoModeracion estado;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static final int MAX_COMENTARIO_LENGTH = 1000;

    private Resena(ResenaId id,
                   AccountId autorId,
                   TipoDestino tipoDestino,
                   ProfesorId profesorId,
                   MateriaId materiaId,
                   int calificacion,
                   String comentario,
                   String materiaCursada,
                   EstadoModeracion estado,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "El ID de la reseña es obligatorio");
        this.autorId = Objects.requireNonNull(autorId, "El autorId es obligatorio");
        this.tipoDestino = Objects.requireNonNull(tipoDestino, "El tipoDestino es obligatorio");
        this.calificacion = Rating.of(calificacion).value();
        this.comentario = validateComentario(comentario);
        this.materiaCursada = (materiaCursada != null && !materiaCursada.isBlank()) ? materiaCursada.strip() : null;
        this.estado = estado != null ? estado : EstadoModeracion.EN_REVISION;
        this.createdAt = createdAt != null ? createdAt : BogotaTime.now();
        this.updatedAt = updatedAt;

        if (tipoDestino == TipoDestino.PROFESOR) {
            if (profesorId == null) {
                throw new DomainException("El profesorId es obligatorio para una reseña a profesor");
            }
            this.profesorId = profesorId;
            this.materiaId = null;
        } else if (tipoDestino == TipoDestino.MATERIA) {
            if (materiaId == null) {
                throw new DomainException("El materiaId es obligatorio para una reseña a materia");
            }
            this.materiaId = materiaId;
            this.profesorId = null;
        } else {
            throw new DomainException("Tipo de destino inválido: " + tipoDestino);
        }
    }

    /**
     * Crea una nueva reseña para un profesor. Inicia en EN_REVISION.
     */
    public static Resena createForProfesor(AccountId autorId,
                                           ProfesorId profesorId,
                                           int calificacion,
                                           String comentario,
                                           String materiaCursada) {
        return new Resena(
                ResenaId.generate(),
                autorId,
                TipoDestino.PROFESOR,
                profesorId,
                null,
                calificacion,
                comentario,
                materiaCursada,
                EstadoModeracion.EN_REVISION,
                BogotaTime.now(),
                null
        );
    }

    /**
     * Crea una nueva reseña para una materia. Inicia en EN_REVISION.
     */
    public static Resena createForMateria(AccountId autorId,
                                          MateriaId materiaId,
                                          int calificacion,
                                          String comentario) {
        return new Resena(
                ResenaId.generate(),
                autorId,
                TipoDestino.MATERIA,
                null,
                materiaId,
                calificacion,
                comentario,
                null,
                EstadoModeracion.EN_REVISION,
                BogotaTime.now(),
                null
        );
    }

    /**
     * Reconstrucción desde persistencia.
     */
    public static Resena of(ResenaId id,
                            AccountId autorId,
                            TipoDestino tipoDestino,
                            ProfesorId profesorId,
                            MateriaId materiaId,
                            int calificacion,
                            String comentario,
                            String materiaCursada,
                            EstadoModeracion estado,
                            LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
        return new Resena(
                id,
                autorId,
                tipoDestino,
                profesorId,
                materiaId,
                calificacion,
                comentario,
                materiaCursada,
                estado,
                createdAt,
                updatedAt
        );
    }

    /**
     * El autor edita su reseña.
     * Supuesto 9: "editar reseña publicada no requiere nueva moderación".
     * Si está archivada, no se puede editar.
     */
    public void updateByAuthor(int nuevaCalificacion, String nuevoComentario, String nuevaMateriaCursada, AccountId editorId) {
        if (!this.autorId.equals(editorId)) {
            throw new ForbiddenException("Solo el autor puede editar su reseña");
        }
        if (this.estado == EstadoModeracion.ARCHIVADO) {
            throw new DomainException("No se puede editar una reseña archivada");
        }
        this.calificacion = Rating.of(nuevaCalificacion).value();
        this.comentario = validateComentario(nuevoComentario);
        if (this.tipoDestino == TipoDestino.PROFESOR) {
            this.materiaCursada = (nuevaMateriaCursada != null && !nuevaMateriaCursada.isBlank())
                    ? nuevaMateriaCursada.strip()
                    : null;
        }
        this.updatedAt = BogotaTime.now();
    }

    public void publish() {
        if (!this.estado.canTransitionTo(EstadoModeracion.PUBLICADO)) {
            throw new DomainException("No se puede publicar la reseña desde el estado actual: " + this.estado);
        }
        this.estado = EstadoModeracion.PUBLICADO;
        this.updatedAt = BogotaTime.now();
    }

    public void archive() {
        if (!this.estado.canTransitionTo(EstadoModeracion.ARCHIVADO)) {
            throw new DomainException("No se puede archivar la reseña desde el estado actual: " + this.estado);
        }
        this.estado = EstadoModeracion.ARCHIVADO;
        this.updatedAt = BogotaTime.now();
    }

    private static String validateComentario(String comentario) {
        if (comentario == null || comentario.isBlank()) {
            throw new DomainException("El comentario de la reseña es obligatorio");
        }
        String stripped = comentario.strip();
        if (stripped.length() > MAX_COMENTARIO_LENGTH) {
            throw new DomainException("El comentario no puede exceder los " + MAX_COMENTARIO_LENGTH + " caracteres");
        }
        return stripped;
    }

    public ResenaId getId() { return id; }
    public AccountId getAutorId() { return autorId; }
    public TipoDestino getTipoDestino() { return tipoDestino; }
    public ProfesorId getProfesorId() { return profesorId; }
    public MateriaId getMateriaId() { return materiaId; }
    public int getCalificacion() { return calificacion; }
    public String getComentario() { return comentario; }
    public String getMateriaCursada() { return materiaCursada; }
    public EstadoModeracion getEstado() { return estado; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

