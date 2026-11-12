package org.infoufps.domain.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de VentureReport")
class VentureReportTest {

    @Test
    @DisplayName("Creación exitosa de un reporte con estado PENDING")
    void createReportSuccess() {
        AccountId reporterId = AccountId.generate();
        VentureId ventureId = VentureId.generate();

        VentureReport report = VentureReport.create(reporterId, ventureId, ReportReason.FRAUDE_O_ESTAFA, "El emprendimiento cobra y no entrega");

        assertNotNull(report.getReportId());
        assertEquals(reporterId, report.getReporterId());
        assertEquals(ventureId, report.getVentureId());
        assertEquals(ReportReason.FRAUDE_O_ESTAFA, report.getReason());
        assertEquals("El emprendimiento cobra y no entrega", report.getComment());
        assertEquals(ReportStatus.PENDING, report.getStatus());
        assertNotNull(report.getCreatedAt());
        assertNull(report.getResolvedAt());
        assertNull(report.getResolvedBy());
    }

    @Test
    @DisplayName("Rechaza comentarios que superen los 500 caracteres")
    void commentExceedsMaxLength() {
        AccountId reporterId = AccountId.generate();
        VentureId ventureId = VentureId.generate();
        String longComment = "C".repeat(501);

        assertThrows(DomainException.class, () -> VentureReport.create(reporterId, ventureId, ReportReason.CONTENIDO_INAPROPIADO, longComment));
    }

    @Test
    @DisplayName("Resolución de un reporte por un administrador")
    void resolveReport() {
        AccountId reporterId = AccountId.generate();
        AccountId adminId = AccountId.generate();
        VentureId ventureId = VentureId.generate();

        VentureReport report = VentureReport.create(reporterId, ventureId, ReportReason.CONTACTO_INVALIDO, null);
        report.resolve(adminId);

        assertEquals(ReportStatus.RESOLVED, report.getStatus());
        assertNotNull(report.getResolvedAt());
        assertEquals(adminId, report.getResolvedBy());
    }
}

