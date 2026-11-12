package org.infoufps.infrastructure.academic.persistence;

import jakarta.persistence.*;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.Facultad;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "profesores", indexes = {
        @Index(name = "idx_profesores_estado_facultad", columnList = "estado, facultad"),
        @Index(name = "idx_profesores_nombre_norm", columnList = "nombre_normalizado"),
        @Index(name = "idx_profesores_email", columnList = "email", unique = true)
})
public class ProfesorJpa {

    @Id
    @Column(name = "profesor_id", length = 36, nullable = false)
    private String profesorId;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "nombre_normalizado", length = 150, nullable = false)
    private String nombreNormalizado;

    @Enumerated(EnumType.STRING)
    @Column(name = "facultad", length = 50, nullable = false)
    private Facultad facultad;

    @Column(name = "email", length = 255, unique = true)
    private String email;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "profesor_materias",
            joinColumns = @JoinColumn(name = "profesor_id"),
            inverseJoinColumns = @JoinColumn(name = "materia_id")
    )
    private Set<MateriaJpa> materias = new LinkedHashSet<>();

    public ProfesorJpa() {
    }

    public String getProfesorId() { return profesorId; }
    public void setProfesorId(String profesorId) { this.profesorId = profesorId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNombreNormalizado() { return nombreNormalizado; }
    public void setNombreNormalizado(String nombreNormalizado) { this.nombreNormalizado = nombreNormalizado; }

    public Facultad getFacultad() { return facultad; }
    public void setFacultad(Facultad facultad) { this.facultad = facultad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public EstadoModeracion getEstado() { return estado; }
    public void setEstado(EstadoModeracion estado) { this.estado = estado; }

    public String getProponenteId() { return proponenteId; }
    public void setProponenteId(String proponenteId) { this.proponenteId = proponenteId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<MateriaJpa> getMaterias() { return materias; }
    public void setMaterias(Set<MateriaJpa> materias) { this.materias = materias; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

