package org.infoufps.infrastructure.calendar.persistence;

import jakarta.persistence.*;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad de Persistencia (JPA/Hibernate) para la tabla 'events'.
 * Mantiene setters mutables y constructor público exigidos por Jakarta Persistence.
 */
@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_events_status_type_start", columnList = "status, event_type, start_date"),
        @Index(name = "idx_events_status_end", columnList = "status, end_date"),
        @Index(name = "idx_events_proposed_by", columnList = "proposed_by")
})
public class EventJpa {

    @Id
    @Column(name = "event_id", length = 36, nullable = false)
    private String eventId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 50, nullable = false)
    private EventType type;

    @Column(name = "location", length = 255, nullable = false)
    private String location;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "info_url", length = 512)
    private String infoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status;

    @Column(name = "proposed_by", length = 36, nullable = false)
    private String proposedBy;

    @Column(name = "created_by_admin", nullable = false)
    private boolean createdByAdmin;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "revision", nullable = false)
    private int revision;

    @Version
    @Column(name = "version")
    private Long version;

    public EventJpa() {
    }

    // ── Getters y Setters ────────────────────────────────────────────────

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInfoUrl() { return infoUrl; }
    public void setInfoUrl(String infoUrl) { this.infoUrl = infoUrl; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getProposedBy() { return proposedBy; }
    public void setProposedBy(String proposedBy) { this.proposedBy = proposedBy; }

    public boolean isCreatedByAdmin() { return createdByAdmin; }
    public void setCreatedByAdmin(boolean createdByAdmin) { this.createdByAdmin = createdByAdmin; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getRevision() { return revision; }
    public void setRevision(int revision) { this.revision = revision; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
