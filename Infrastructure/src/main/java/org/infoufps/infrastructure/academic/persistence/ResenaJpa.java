package org.infoufps.infrastructure.academic.persistence;

import jakarta.persistence.*;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoDestino;

import java.time.LocalDateTime;

@Entity
@Table(name = "resenas", indexes = {
        @Index(name = "idx_resenas_profesor_estado", columnList = "profesor_id, estado"),
        @Index(name = "idx_resenas_materia_estado", columnList = "materia_id, estado"),
        @Index(name = "idx_resenas_autor_estado", columnList = "autor_id, estado"),
        @Index(name = "idx_resenas_created_at", columnList = "created_at")
})
public class ResenaJpa {

    @Id
    @Column(name = "resena_id", length = 36, nullable = false)
    private String resenaId;

    @Column(name = "autor_id", length = 36, nullable = false)
    private String autorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_destino", length = 20, nullable = false)
    private TipoDestino tipoDestino;

    @Column(name = "profesor_id", length = 36)
    private String profesorId;

    @Column(name = "materia_id", length = 36)
    private String materiaId;

    @Column(name = "calificacion", nullable = false)
    private int calificacion;

    @Column(name = "comentario", length = 1000, nullable = false)
    private String comentario;

    @Column(name = "materia_cursada", length = 150)
    private String materiaCursada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30, nullable = false)
    private EstadoModeracion estado;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    public ResenaJpa() {
    }

    public String getResenaId() { return resenaId; }
    public void setResenaId(String resenaId) { this.resenaId = resenaId; }

    public String getAutorId() { return autorId; }
    public void setAutorId(String autorId) { this.autorId = autorId; }

    public TipoDestino getTipoDestino() { return tipoDestino; }
    public void setTipoDestino(TipoDestino tipoDestino) { this.tipoDestino = tipoDestino; }

    public String getProfesorId() { return profesorId; }
    public void setProfesorId(String profesorId) { this.profesorId = profesorId; }

    public String getMateriaId() { return materiaId; }
    public void setMateriaId(String materiaId) { this.materiaId = materiaId; }

    public int getCalificacion() { return calificacion; }
    public void setCalificacion(int calificacion) { this.calificacion = calificacion; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getMateriaCursada() { return materiaCursada; }
    public void setMateriaCursada(String materiaCursada) { this.materiaCursada = materiaCursada; }

    public EstadoModeracion getEstado() { return estado; }
    public void setEstado(EstadoModeracion estado) { this.estado = estado; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

