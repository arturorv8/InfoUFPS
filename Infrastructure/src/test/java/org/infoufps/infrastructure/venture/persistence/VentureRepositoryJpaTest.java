package org.infoufps.infrastructure.venture.persistence;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.venture.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de mapeo de repositorios JPA de Venture")
class VentureRepositoryJpaTest {

    private final VentureRepositoryJpa ventureRepository = new VentureRepositoryJpa();
    private final VentureReportRepositoryJpa reportRepository = new VentureReportRepositoryJpa();

    @Test
    @DisplayName("VentureRepositoryJpa: mapeo toDomain y toEntity bidireccional")
    void ventureBidirectionalMapping() {
        AccountId owner = AccountId.generate();
        Contact contact = Contact.of(ContactType.WHATSAPP, "3001234567");
        Set<ServiceMode> modes = Set.of(ServiceMode.PRESENCIAL, ServiceMode.DOMICILIO);

        Venture original = Venture.create(
                "Hamburguesas UFPS",
                "Comida rápida artesanal en el campus",
                VentureCategory.COMIDA_Y_BEBIDAS,
                owner,
                contact,
                modes,
                "Edificio Aulas Sur"
        );
        original.publish();

        // toEntity
        VentureJpa entity = toEntityHelper(original);
        assertEquals(original.getId().value(), entity.getVentureId());
        assertEquals(original.getName(), entity.getName());
        assertEquals(original.getDescription(), entity.getDescription());
        assertEquals(original.getCategory(), entity.getCategory());
        assertEquals(original.getOwnerId().value(), entity.getOwnerId());
        assertEquals(ContactType.WHATSAPP, entity.getContactType());
        assertEquals("+573001234567", entity.getContactValue());
        assertEquals(2, entity.getServiceModes().size());
        assertEquals("Edificio Aulas Sur", entity.getLocation());
        assertTrue(entity.isActive());
        assertEquals(Status.PUBLISHED, entity.getStatus());
        assertNotNull(entity.getPublishedAt());
        assertEquals("hamburguesas ufps", entity.getNameSearch());

        // toDomain
        Venture mapped = toDomainHelper(entity);
        assertEquals(original.getId(), mapped.getId());
        assertEquals(original.getName(), mapped.getName());
        assertEquals(original.getDescription(), mapped.getDescription());
        assertEquals(original.getCategory(), mapped.getCategory());
        assertEquals(original.getOwnerId(), mapped.getOwnerId());
        assertEquals(original.getContact(), mapped.getContact());
        assertEquals(original.getServiceModes(), mapped.getServiceModes());
        assertEquals(original.getLocation(), mapped.getLocation());
        assertEquals(original.isActive(), mapped.isActive());
        assertEquals(original.getStatus(), mapped.getStatus());
        assertEquals(original.getPublishedAt(), mapped.getPublishedAt());
        assertEquals(original.getNameSearch(), mapped.getNameSearch());
    }

    @Test
    @DisplayName("VentureReportRepositoryJpa: mapeo toDomain y toEntity bidireccional")
    void reportBidirectionalMapping() {
        AccountId reporterId = AccountId.generate();
        AccountId adminId = AccountId.generate();
        VentureId ventureId = VentureId.generate();

        VentureReport report = VentureReport.create(
                reporterId,
                ventureId,
                ReportReason.FRAUDE_O_ESTAFA,
                "No entrega los productos acordados"
        );
        report.resolve(adminId);

        VentureReportJpa entity = toReportEntityHelper(report);
        assertEquals(report.getReportId(), entity.getReportId());
        assertEquals(reporterId.value(), entity.getReporterId());
        assertEquals(ventureId.value(), entity.getVentureId());
        assertEquals(ReportReason.FRAUDE_O_ESTAFA, entity.getReason());
        assertEquals(ReportStatus.RESOLVED, entity.getStatus());
        assertEquals(adminId.value(), entity.getResolvedBy());
        assertNotNull(entity.getResolvedAt());

        VentureReport mapped = toReportDomainHelper(entity);
        assertEquals(report.getReportId(), mapped.getReportId());
        assertEquals(report.getReporterId(), mapped.getReporterId());
        assertEquals(report.getVentureId(), mapped.getVentureId());
        assertEquals(report.getReason(), mapped.getReason());
        assertEquals(report.getComment(), mapped.getComment());
        assertEquals(ReportStatus.RESOLVED, mapped.getStatus());
        assertEquals(adminId, mapped.getResolvedBy());
        assertEquals(report.getResolvedAt(), mapped.getResolvedAt());
    }

    private VentureJpa toEntityHelper(Venture v) {
        VentureJpa entity = new VentureJpa();
        entity.setVentureId(v.getId().value());
        entity.setName(v.getName());
        entity.setDescription(v.getDescription());
        entity.setCategory(v.getCategory());
        entity.setOwnerId(v.getOwnerId().value());
        entity.setContactType(v.getContact().type());
        entity.setContactValue(v.getContact().value());
        entity.setServiceModes(new java.util.HashSet<>(v.getServiceModes()));
        entity.setLocation(v.getLocation());
        entity.setActive(v.isActive());
        entity.setStatus(v.getStatus());
        entity.setCreatedAt(v.getCreatedAt());
        entity.setUpdatedAt(v.getUpdatedAt());
        entity.setPublishedAt(v.getPublishedAt());
        entity.setNameSearch(v.getNameSearch());
        return entity;
    }

    private Venture toDomainHelper(VentureJpa e) {
        Contact contact = Contact.of(e.getContactType(), e.getContactValue());
        return Venture.of(
                VentureId.of(e.getVentureId()),
                e.getName(),
                e.getDescription(),
                e.getCategory(),
                AccountId.of(e.getOwnerId()),
                contact,
                java.util.EnumSet.copyOf(e.getServiceModes()),
                e.getLocation(),
                e.isActive(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getPublishedAt(),
                e.getNameSearch()
        );
    }

    private VentureReportJpa toReportEntityHelper(VentureReport r) {
        VentureReportJpa entity = new VentureReportJpa();
        entity.setReportId(r.getReportId());
        entity.setReporterId(r.getReporterId().value());
        entity.setVentureId(r.getVentureId().value());
        entity.setReason(r.getReason());
        entity.setComment(r.getComment());
        entity.setStatus(r.getStatus());
        entity.setCreatedAt(r.getCreatedAt());
        entity.setResolvedAt(r.getResolvedAt());
        entity.setResolvedBy(r.getResolvedBy() != null ? r.getResolvedBy().value() : null);
        return entity;
    }

    private VentureReport toReportDomainHelper(VentureReportJpa e) {
        return VentureReport.of(
                e.getReportId(),
                AccountId.of(e.getReporterId()),
                VentureId.of(e.getVentureId()),
                e.getReason(),
                e.getComment(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getResolvedAt(),
                e.getResolvedBy() != null ? AccountId.of(e.getResolvedBy()) : null
        );
    }
}

