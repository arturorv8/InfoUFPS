package org.infoufps.infrastructure.academic.persistence;

import jakarta.persistence.*;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoMateria;

import java.time.LocalDateTime;

@Entity
@Table(name = "materias", indexes = {
        @Index(name = "idx_materias_estado_tipo_creditos", columnList = "estado, tipo, creditos"),
        @Index(name = "idx_materias_codigo", columnList = "codigo", unique = true),
        @Index(name = "idx_materias_nombre_norm", columnList = "nombre_normalizado")
})
public class MateriaJpa {

    @Id
    @Column(name = "materia_id", length = 36, nullable = false)
    private String materiaId;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "nombre_normalizado", length = 150, nullable = false)
    private String nombreNormalizado;

    @Column(name = "codigo", length = 30, unique = true)
    private String codigo;

    @Column(name = "creditos")
    private Integer creditos;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 30)
    private TipoMateria tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30, nullable = false)
    private EstadoModeracion estado;

    @Column(name = "proponente_id", length = 36)
    private String proponenteId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    public MateriaJpa() {
    }

    public String getMateriaId() { return materiaId; }
    public void setMateriaId(String materiaId) { this.materiaId = materiaId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNombreNormalizado() { return nombreNormalizado; }
    public void setNombreNormalizado(String nombreNormalizado) { this.nombreNormalizado = nombreNormalizado; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public Integer getCreditos() { return creditos; }
    public void setCreditos(Integer creditos) { this.creditos = creditos; }

    public TipoMateria getTipo() { return tipo; }
    public void setTipo(TipoMateria tipo) { this.tipo = tipo; }

    public EstadoModeracion getEstado() { return estado; }
    public void setEstado(EstadoModeracion estado) { this.estado = estado; }

    public String getProponenteId() { return proponenteId; }
    public void setProponenteId(String proponenteId) { this.proponenteId = proponenteId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

