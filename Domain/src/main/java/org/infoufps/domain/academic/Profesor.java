package org.infoufps.domain.academic;

import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.valueobjects.Email;
import org.infoufps.domain.academic.MateriaId;
import org.infoufps.domain.academic.ProfesorId;

import java.time.LocalDateTime;
import java.util.*;

public class Profesor {

    private final ProfesorId id;
    private String nombre;
    private String nombreNormalizado;
    private Facultad facultad;
    private String email;
    private EstadoModeracion estado;
    private final AccountId proponenteId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<MateriaId> materias;

    private Profesor(ProfesorId id,
                     String nombre,
                     String nombreNormalizado,
                     Facultad facultad,
                     String email,
                     EstadoModeracion estado,
                     AccountId proponenteId,
                     LocalDateTime createdAt,
                     LocalDateTime updatedAt,
                     Set<MateriaId> materias) {
        this.id = Objects.requireNonNull(id, "El ID del profesor es obligatorio");
        this.nombre = validateNombre(nombre);
        this.nombreNormalizado = (nombreNormalizado != null && !nombreNormalizado.isBlank())
                ? nombreNormalizado
                : TextNormalizer.normalizeAndSortWords(nombre);
        this.facultad = facultad != null ? facultad : Facultad.NO_ESPECIFICADA;
        this.email = email != null && !email.isBlank() ? Email.normalize(email) : null;
        this.estado = estado != null ? estado : EstadoModeracion.EN_REVISION;
        this.proponenteId = proponenteId;
        this.createdAt = createdAt != null ? createdAt : BogotaTime.now();
        this.updatedAt = updatedAt;
        this.materias = materias != null ? new LinkedHashSet<>(materias) : new LinkedHashSet<>();
    }

    /**
     * Propuesta de un profesor creada por un usuario autenticado. Inicia siempre en EN_REVISION.
     */
    public static Profesor propose(String nombre, Facultad facultad, String email, AccountId proponenteId) {
        if (proponenteId == null) {
            throw new DomainException("El proponente es obligatorio al proponer un profesor");
        }
        return new Profesor(
                ProfesorId.generate(),
                nombre,
                null,
                facultad,
                email,
                EstadoModeracion.EN_REVISION,
                proponenteId,
                BogotaTime.now(),
                null,
                new LinkedHashSet<>()
        );
    }

    /**
     * Creación directa por un administrador. Inicia en PUBLICADO.
     */
    public static Profesor createByAdmin(String nombre, Facultad facultad, String email) {
        return new Profesor(
                ProfesorId.generate(),
                nombre,
                null,
                facultad,
                email,
                EstadoModeracion.PUBLICADO,
                null,
                BogotaTime.now(),
                null,
                new LinkedHashSet<>()
        );
    }

    /**
     * Reconstrucción desde infraestructura/persistencia.
     */
    public static Profesor of(ProfesorId id,
                              String nombre,
                              String nombreNormalizado,
                              Facultad facultad,
                              String email,
                              EstadoModeracion estado,
                              AccountId proponenteId,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt,
                              Set<MateriaId> materias) {
        return new Profesor(
                id,
                nombre,
                nombreNormalizado,
                facultad,
                email,
                estado,
                proponenteId,
                createdAt,
                updatedAt,
                materias
        );
    }

    public void updateProposal(String nuevoNombre, Facultad nuevaFacultad, String nuevoEmail, AccountId editorId) {
        if (this.estado != EstadoModeracion.EN_REVISION) {
            throw new DomainException("Solo se pueden editar propuestas en revisión");
        }
        if (this.proponenteId == null || !this.proponenteId.equals(editorId)) {
            throw new ForbiddenException("No tienes permiso para editar esta propuesta");
        }
        this.nombre = validateNombre(nuevoNombre);
        this.nombreNormalizado = TextNormalizer.normalizeAndSortWords(nuevoNombre);
        this.facultad = nuevaFacultad != null
                ? nuevaFacultad
                : (this.facultad != null ? this.facultad : Facultad.NO_ESPECIFICADA);
        this.email = nuevoEmail != null && !nuevoEmail.isBlank() ? Email.normalize(nuevoEmail) : null;
        this.updatedAt = BogotaTime.now();
    }

    public void adminUpdate(String nuevoNombre, Facultad nuevaFacultad, String nuevoEmail) {
        if (this.estado == EstadoModeracion.ARCHIVADO) {
            throw new DomainException("No se puede editar un profesor archivado");
        }
        this.nombre = validateNombre(nuevoNombre);
        this.nombreNormalizado = TextNormalizer.normalizeAndSortWords(nuevoNombre);
        if (nuevaFacultad != null) {
            this.facultad = nuevaFacultad;
        }
        this.email = nuevoEmail != null && !nuevoEmail.isBlank() ? Email.normalize(nuevoEmail) : null;
        this.updatedAt = BogotaTime.now();
    }

    public void publish() {
        if (!this.estado.canTransitionTo(EstadoModeracion.PUBLICADO)) {
            throw new DomainException("No se puede publicar el profesor desde el estado actual: " + this.estado);
        }
        this.estado = EstadoModeracion.PUBLICADO;
        this.updatedAt = BogotaTime.now();
    }

    public void archive() {
        if (!this.estado.canTransitionTo(EstadoModeracion.ARCHIVADO)) {
            throw new DomainException("No se puede archivar el profesor desde el estado actual: " + this.estado);
        }
        this.estado = EstadoModeracion.ARCHIVADO;
        this.updatedAt = BogotaTime.now();
    }

    public void assignMateria(MateriaId materiaId) {
        Objects.requireNonNull(materiaId, "materiaId no puede ser nulo");
        this.materias.add(materiaId);
        this.updatedAt = BogotaTime.now();
    }

    public void removeMateria(MateriaId materiaId) {
        if (materiaId != null) {
            this.materias.remove(materiaId);
            this.updatedAt = BogotaTime.now();
        }
    }

    public void setMaterias(Set<MateriaId> materias) {
        this.materias = materias != null ? new LinkedHashSet<>(materias) : new LinkedHashSet<>();
        this.updatedAt = BogotaTime.now();
    }

    private static String validateNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("El nombre del profesor es obligatorio");
        }
        String stripped = nombre.strip();
        if (stripped.length() < 2 || stripped.length() > 150) {
            throw new DomainException("El nombre del profesor debe tener entre 2 y 150 caracteres");
        }
        return stripped;
    }

    public ProfesorId getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNombreNormalizado() { return nombreNormalizado; }
    public Facultad getFacultad() { return facultad; }
    public String getEmail() { return email; }
    public EstadoModeracion getEstado() { return estado; }
    public AccountId getProponenteId() { return proponenteId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Set<MateriaId> getMaterias() { return Collections.unmodifiableSet(materias); }
}

