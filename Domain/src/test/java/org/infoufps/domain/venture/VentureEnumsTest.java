package org.infoufps.domain.venture;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Enums del módulo Venture")
class VentureEnumsTest {

    @Test
    @DisplayName("VentureCategory: resolución válida y rechazo de desconocidos")
    void ventureCategory() {
        assertEquals(VentureCategory.COMIDA_Y_BEBIDAS, VentureCategory.fromString("COMIDA_Y_BEBIDAS"));
        assertEquals(VentureCategory.COMIDA_Y_BEBIDAS, VentureCategory.fromString("comida_y_bebidas"));
        assertNull(VentureCategory.fromString(""));
        assertNull(VentureCategory.fromString(null));

        assertThrows(DomainException.class, () -> VentureCategory.fromString("CATEGORIA_INEXISTENTE"));
    }

    @Test
    @DisplayName("ServiceMode: resolución válida y rechazo de desconocidos")
    void serviceMode() {
        assertEquals(ServiceMode.PRESENCIAL, ServiceMode.fromString("PRESENCIAL"));
        assertEquals(ServiceMode.DOMICILIO, ServiceMode.fromString("domicilio"));
        assertNull(ServiceMode.fromString(""));
        assertNull(ServiceMode.fromString(null));

        assertThrows(DomainException.class, () -> ServiceMode.fromString("TELEPATIA"));
    }

    @Test
    @DisplayName("ContactType: resolución válida y rechazo de desconocidos")
    void contactType() {
        assertEquals(ContactType.WHATSAPP, ContactType.fromString("whatsapp"));
        assertEquals(ContactType.INSTAGRAM, ContactType.fromString("INSTAGRAM"));
        assertNull(ContactType.fromString(""));
        assertNull(ContactType.fromString(null));

        assertThrows(DomainException.class, () -> ContactType.fromString("TIKTOK"));
    }

    @Test
    @DisplayName("ReportReason: resolución válida y rechazo de desconocidos")
    void reportReason() {
        assertEquals(ReportReason.FRAUDE_O_ESTAFA, ReportReason.fromString("fraude_o_estafa"));
        assertNull(ReportReason.fromString(""));
        assertNull(ReportReason.fromString(null));

        assertThrows(DomainException.class, () -> ReportReason.fromString("SPAM_NO_DESEADO"));
    }
}

