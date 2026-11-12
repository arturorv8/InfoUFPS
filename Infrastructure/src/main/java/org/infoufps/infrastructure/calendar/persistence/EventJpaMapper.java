package org.infoufps.infrastructure.calendar.persistence;

import org.infoufps.domain.calendar.Event;
import org.infoufps.infrastructure.calendar.persistence.EventJpa;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;
import org.infoufps.domain.calendar.EventSchedule;

/**
 * Mapeador compartido entre los repositorios JPA de eventos y favoritos.
 */
final class EventJpaMapper {

    private EventJpaMapper() {
    }

    static EventJpa toEntity(Event event) {
        EventJpa entity = new EventJpa();
        entity.setEventId(event.getId().value());
        entity.setTitle(event.getTitle());
        entity.setType(event.getType());
        entity.setLocation(event.getLocation());
        entity.setStartDate(event.getStartDate());
        entity.setEndDate(event.getEndDate());
        entity.setStartTime(event.getStartTime());
        entity.setEndTime(event.getEndTime());
        entity.setDescription(event.getDescription());
        entity.setInfoUrl(event.getInfoUrl());
        entity.setStatus(event.getStatus());
        entity.setProposedBy(event.getProposedBy().value());
        entity.setCreatedByAdmin(event.isCreatedByAdmin());
        entity.setPublishedAt(event.getPublishedAt());
        entity.setCreatedAt(event.getCreatedAt());
        entity.setUpdatedAt(event.getUpdatedAt());
        entity.setRevision(event.getRevision());
        return entity;
    }

    static Event toDomain(EventJpa jpa) {
        return Event.of(
                EventId.of(jpa.getEventId()),
                jpa.getTitle(),
                jpa.getType(),
                jpa.getLocation(),
                EventSchedule.of(jpa.getStartDate(), jpa.getEndDate(), jpa.getStartTime(), jpa.getEndTime()),
                jpa.getDescription(),
                jpa.getInfoUrl(),
                jpa.getStatus(),
                AccountId.of(jpa.getProposedBy()),
                jpa.isCreatedByAdmin(),
                jpa.getPublishedAt(),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt(),
                jpa.getRevision()
        );
    }
}
