package org.infoufps.domain.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad de Dominio que modela un emprendimiento en el módulo La Vitrina de InfoUFPS.
 */
public class Venture {

    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_LOCATION_LENGTH = 255;

    private final VentureId id;
    private String name;
    private String description;
    private VentureCategory category;
    private final AccountId ownerId;
    private Contact contact;
    private Set<ServiceMode> serviceModes;
    private String location;
    private boolean active;
    private Status status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private String nameSearch;

    private Venture(VentureId id,
                    String name,
                    String description,
                    VentureCategory category,
                    AccountId ownerId,
                    Contact contact,
                    Set<ServiceMode> serviceModes,
                    String location,
                    boolean active,
                    Status status,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt,
                    LocalDateTime publishedAt,
                    String nameSearch) {
        this.id = Objects.requireNonNull(id, "El id del emprendimiento es obligatorio");
        this.name = validateName(name);
        this.description = validateDescription(description);
        this.category = Objects.requireNonNull(category, "La categoría es obligatoria");
        this.ownerId = Objects.requireNonNull(ownerId, "El id del propietario es obligatorio");
        this.contact = Objects.requireNonNull(contact, "El contacto es obligatorio");
        this.serviceModes = validateServiceModes(serviceModes);
        this.location = validateLocation(location);
        this.active = active;
        this.status = Objects.requireNonNull(status, "El estado es obligatorio");
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        this.updatedAt = Objects.requireNonNull(updatedAt, "La fecha de edición es obligatoria");
        this.publishedAt = publishedAt;
        this.nameSearch = nameSearch != null ? nameSearch : TextNormalizer.normalize(this.name);
    }

    /**
     * Fábrica para crear un nuevo emprendimiento.
     * Inicia en estado {@link Status#UNDER_REVIEW} y activo.
     */
    public static Venture create(String name,
                                 String description,
                                 VentureCategory category,
                                 AccountId ownerId,
                                 Contact contact,
                                 Set<ServiceMode> serviceModes,
                                 String location) {
        LocalDateTime now = BogotaTime.now();
        String validName = validateName(name);
        String nameSearch = TextNormalizer.normalize(validName);

        return new Venture(
                VentureId.generate(),
                validName,
                description,
                category,
                ownerId,
                contact,
                serviceModes,
                location,
                true,
                Status.UNDER_REVIEW,
                now,
                now,
                null,
                nameSearch
        );
    }

    /**
     * Fábrica para reconstruir un emprendimiento desde la persistencia.
     */
    public static Venture of(VentureId id,
                             String name,
                             String description,
                             VentureCategory category,
                             AccountId ownerId,
                             Contact contact,
                             Set<ServiceMode> serviceModes,
                             String location,
                             boolean active,
                             Status status,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt,
                             LocalDateTime publishedAt,
                             String nameSearch) {
        return new Venture(
                id,
                name,
                description,
                category,
                ownerId,
                contact,
                serviceModes,
                location,
                active,
                status,
                createdAt,
                updatedAt,
                publishedAt,
                nameSearch
        );
    }

    // ── Validaciones ──────────────────────────────────────────────────

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre del emprendimiento es obligatorio");
        }
        String trimmed = name.strip();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new DomainException("El nombre no puede superar los " + MAX_NAME_LENGTH + " caracteres");
        }
        return trimmed;
    }

    private static String validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new DomainException("La descripción es obligatoria");
        }
        String trimmed = description.strip();
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            throw new DomainException("La descripción no puede superar los " + MAX_DESCRIPTION_LENGTH + " caracteres");
        }
        return trimmed;
    }

    private static String validateLocation(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        String trimmed = location.strip();
        if (trimmed.length() > MAX_LOCATION_LENGTH) {
            throw new DomainException("La ubicación no puede superar los " + MAX_LOCATION_LENGTH + " caracteres");
        }
        return trimmed;
    }

    private static Set<ServiceMode> validateServiceModes(Set<ServiceMode> modes) {
        if (modes == null || modes.isEmpty()) {
            throw new DomainException("Debe seleccionar al menos un modo de servicio");
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(modes));
    }

    // ── Reglas de Dominio ─────────────────────────────────────────────

    /**
     * Un emprendimiento es visible públicamente solo si está publicado y activo.
     */
    public boolean isVisible() {
        return this.status == Status.PUBLISHED && this.active;
    }

    /**
     * Edita los datos del emprendimiento.
     * Solo puede editar el dueño o un administrador.
     * Si el emprendimiento estaba publicado y se cambia el contacto, vuelve automáticamente a UNDER_REVIEW.
     */
    public void update(String newName,
                       String newDescription,
                       VentureCategory newCategory,
                       Contact newContact,
                       Set<ServiceMode> newServiceModes,
                       String newLocation,
                       AccountId requesterId,
                       boolean isAdmin) {
        assertCanEdit(requesterId, isAdmin);

        String validName = validateName(newName);
        String validDescription = validateDescription(newDescription);
        VentureCategory validCategory = Objects.requireNonNull(newCategory, "La categoría es obligatoria");
        Contact validContact = Objects.requireNonNull(newContact, "El contacto es obligatorio");
        Set<ServiceMode> validModes = validateServiceModes(newServiceModes);
        String validLocation = validateLocation(newLocation);

        // Si estaba publicado y cambia el contacto, vuelve a revisión
        if (this.status == Status.PUBLISHED && !this.contact.equals(validContact)) {
            if (!this.status.canTransitionTo(Status.UNDER_REVIEW)) {
                throw new DomainException("No se puede devolver el emprendimiento a revisión desde " + this.status);
            }
            this.status = Status.UNDER_REVIEW;
        }

        this.name = validName;
        this.description = validDescription;
        this.category = validCategory;
        this.contact = validContact;
        this.serviceModes = validModes;
        this.location = validLocation;
        this.nameSearch = TextNormalizer.normalize(validName);
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Activa el emprendimiento para hacerlo visible (si está publicado).
     */
    public void activate(AccountId requesterId, boolean isAdmin) {
        assertCanEdit(requesterId, isAdmin);
        this.active = true;
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Desactiva temporalmente el emprendimiento sin despublicarlo ni archivarlo.
     */
    public void deactivate(AccountId requesterId, boolean isAdmin) {
        assertCanEdit(requesterId, isAdmin);
        this.active = false;
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Publica el emprendimiento (acción de moderación admin).
     */
    public void publish() {
        if (!this.status.canTransitionTo(Status.PUBLISHED)) {
            throw new DomainException("No se puede publicar un emprendimiento desde el estado actual: " + this.status);
        }
        this.status = Status.PUBLISHED;
        if (this.publishedAt == null) {
            this.publishedAt = BogotaTime.now();
        }
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Archiva o retira el emprendimiento del sistema (acción de moderación admin).
     */
    public void archive() {
        if (!this.status.canTransitionTo(Status.ARCHIVED)) {
            throw new DomainException("No se puede archivar un emprendimiento desde el estado actual: " + this.status);
        }
        this.status = Status.ARCHIVED;
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Devuelve el emprendimiento a revisión (por ejemplo cuando acumula el umbral de reportes pendientes).
     */
    public void returnToReview() {
        if (this.status == Status.PUBLISHED) {
            if (!this.status.canTransitionTo(Status.UNDER_REVIEW)) {
                throw new DomainException("No se puede devolver a revisión desde " + this.status);
            }
            this.status = Status.UNDER_REVIEW;
            this.updatedAt = BogotaTime.now();
        }
    }

    private void assertCanEdit(AccountId requesterId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (requesterId == null || !this.ownerId.equals(requesterId)) {
            throw new ForbiddenException("No tienes permiso para modificar este emprendimiento");
        }
    }

    // ── Getters ───────────────────────────────────────────────────────

    public VentureId getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public VentureCategory getCategory() { return category; }
    public AccountId getOwnerId() { return ownerId; }
    public Contact getContact() { return contact; }
    public Set<ServiceMode> getServiceModes() { return serviceModes; }
    public String getLocation() { return location; }
    public boolean isActive() { return active; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public String getNameSearch() { return nameSearch; }
}

