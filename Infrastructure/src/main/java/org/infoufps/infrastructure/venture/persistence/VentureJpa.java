package org.infoufps.infrastructure.venture.persistence;

import jakarta.persistence.*;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.venture.ContactType;
import org.infoufps.domain.venture.ServiceMode;
import org.infoufps.domain.venture.VentureCategory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para la tabla 'ventures'.
 */
@Entity
@Table(name = "ventures",
        indexes = {
                @Index(name = "idx_ventures_status_active_category", columnList = "status, active, category"),
                @Index(name = "idx_ventures_name_search", columnList = "name_search"),
                @Index(name = "idx_ventures_owner_id", columnList = "owner_id"),
                @Index(name = "idx_ventures_contact_value", columnList = "contact_value")
        })
public class VentureJpa {

    @Id
    @Column(name = "venture_id", length = 36, nullable = false)
    private String ventureId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 1000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50, nullable = false)
    private VentureCategory category;

    @Column(name = "owner_id", length = 36, nullable = false)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", length = 20, nullable = false)
    private ContactType contactType;

    @Column(name = "contact_value", length = 512, nullable = false)
    private String contactValue;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "venture_service_modes", joinColumns = @JoinColumn(name = "venture_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service_mode", length = 30, nullable = false)
    private Set<ServiceMode> serviceModes = new HashSet<>();

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.UNDER_REVIEW;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "name_search", length = 255, nullable = false)
    private String nameSearch;

    @Version
    @Column(name = "version")
    private Long version;

    public VentureJpa() {}

    public String getVentureId() { return ventureId; }
    public void setVentureId(String ventureId) { this.ventureId = ventureId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public VentureCategory getCategory() { return category; }
    public void setCategory(VentureCategory category) { this.category = category; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public ContactType getContactType() { return contactType; }
    public void setContactType(ContactType contactType) { this.contactType = contactType; }

    public String getContactValue() { return contactValue; }
    public void setContactValue(String contactValue) { this.contactValue = contactValue; }

    public Set<ServiceMode> getServiceModes() { return serviceModes; }
    public void setServiceModes(Set<ServiceMode> serviceModes) { this.serviceModes = serviceModes; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public String getNameSearch() { return nameSearch; }
    public void setNameSearch(String nameSearch) { this.nameSearch = nameSearch; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

