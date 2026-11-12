package org.infoufps.domain.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de la Entidad Venture")
class VentureTest {

    private AccountId ownerId;
    private AccountId otherUserId;
    private Contact contact;
    private Set<ServiceMode> serviceModes;

    @BeforeEach
    void setUp() {
        ownerId = AccountId.generate();
        otherUserId = AccountId.generate();
        contact = Contact.of(ContactType.WHATSAPP, "3001234567");
        serviceModes = Set.of(ServiceMode.PRESENCIAL, ServiceMode.DOMICILIO);
    }

    @Test
    @DisplayName("Creación exitosa con valores iniciales por defecto")
    void createSuccess() {
        Venture v = Venture.create(
                "Café Universitario",
                "Café gourmet y postres artesanales para la comunidad",
                VentureCategory.COMIDA_Y_BEBIDAS,
                ownerId,
                contact,
                serviceModes,
                "Frente a la biblioteca"
        );

        assertNotNull(v.getId());
        assertEquals("Café Universitario", v.getName());
        assertEquals("cafe universitario", v.getNameSearch());
        assertEquals(Status.UNDER_REVIEW, v.getStatus());
        assertTrue(v.isActive());
        assertFalse(v.isVisible(), "No debe ser visible si está UNDER_REVIEW");
        assertNotNull(v.getCreatedAt());
        assertNotNull(v.getUpdatedAt());
        assertNull(v.getPublishedAt());
    }

    @Test
    @DisplayName("Validaciones de campos en creación")
    void createValidations() {
        // Nombre vacío o largo
        assertThrows(DomainException.class, () -> Venture.create("", "Desc", VentureCategory.OTROS, ownerId, contact, serviceModes, null));
        assertThrows(DomainException.class, () -> Venture.create("A".repeat(101), "Desc", VentureCategory.OTROS, ownerId, contact, serviceModes, null));

        // Descripción vacía o larga
        assertThrows(DomainException.class, () -> Venture.create("Nombre", "", VentureCategory.OTROS, ownerId, contact, serviceModes, null));
        assertThrows(DomainException.class, () -> Venture.create("Nombre", "A".repeat(1001), VentureCategory.OTROS, ownerId, contact, serviceModes, null));

        // Ubicación mayor a 255
        assertThrows(DomainException.class, () -> Venture.create("Nombre", "Desc", VentureCategory.OTROS, ownerId, contact, serviceModes, "A".repeat(256)));

        // Modos de servicio vacíos
        assertThrows(DomainException.class, () -> Venture.create("Nombre", "Desc", VentureCategory.OTROS, ownerId, contact, Collections.emptySet(), null));
    }

    @Test
    @DisplayName("Visibilidad: visible solo si está PUBLISHED y activo")
    void visibilityRule() {
        Venture v = Venture.create("Mi Tienda", "Desc", VentureCategory.MODA_Y_ACCESORIOS, ownerId, contact, serviceModes, null);
        assertFalse(v.isVisible());

        v.publish();
        assertTrue(v.isVisible(), "Debe ser visible al estar PUBLISHED y active=true");

        v.deactivate(ownerId, false);
        assertFalse(v.isVisible(), "No debe ser visible si active=false");

        v.activate(ownerId, false);
        assertTrue(v.isVisible());

        v.archive();
        assertFalse(v.isVisible(), "No debe ser visible si está ARCHIVED");
    }

    @Test
    @DisplayName("Permisos de edición: solo el dueño o un admin pueden editar")
    void updatePermissions() {
        Venture v = Venture.create("Tienda", "Desc", VentureCategory.TECNOLOGIA, ownerId, contact, serviceModes, null);

        // Intento de edición por otro usuario sin rol admin -> ForbiddenException
        assertThrows(ForbiddenException.class, () -> v.update(
                "Nuevo Nombre", "Nueva Desc", VentureCategory.TECNOLOGIA, contact, serviceModes, null, otherUserId, false
        ));

        // Edición por el dueño -> permitido
        assertDoesNotThrow(() -> v.update(
                "Nuevo Nombre", "Nueva Desc", VentureCategory.TECNOLOGIA, contact, serviceModes, null, ownerId, false
        ));
        assertEquals("Nuevo Nombre", v.getName());

        // Edición por un admin -> permitido
        assertDoesNotThrow(() -> v.update(
                "Nombre Admin", "Desc Admin", VentureCategory.TECNOLOGIA, contact, serviceModes, null, otherUserId, true
        ));
        assertEquals("Nombre Admin", v.getName());
    }

    @Test
    @DisplayName("Regla clave: cambiar contacto en un emprendimiento publicado lo devuelve a UNDER_REVIEW")
    void changingContactOnPublishedVentureReturnsToUnderReview() {
        Venture v = Venture.create("Tienda", "Desc", VentureCategory.SALUD_Y_BIENESTAR, ownerId, contact, serviceModes, null);
        v.publish();
        assertEquals(Status.PUBLISHED, v.getStatus());

        Contact newContact = Contact.of(ContactType.INSTAGRAM, "tienda_saludable");
        v.update("Tienda", "Desc", VentureCategory.SALUD_Y_BIENESTAR, newContact, serviceModes, null, ownerId, false);

        assertEquals(Status.UNDER_REVIEW, v.getStatus(), "Cambiar el contacto debe devolver el estado a UNDER_REVIEW");
    }

    @Test
    @DisplayName("Regla clave: cambiar otros campos en un emprendimiento publicado NO cambia su estado")
    void changingOtherFieldsOnPublishedVentureKeepsPublishedStatus() {
        Venture v = Venture.create("Tienda", "Desc", VentureCategory.SERVICIOS_Y_TUTORIAS, ownerId, contact, serviceModes, null);
        v.publish();
        assertEquals(Status.PUBLISHED, v.getStatus());

        // Actualizar nombre, descripción, modos y ubicación manteniendo el mismo contacto
        v.update("Nuevo Título de Tutoría", "Nueva descripción extendida", VentureCategory.SERVICIOS_Y_TUTORIAS,
                contact, Set.of(ServiceMode.VIRTUAL), "Campus Central", ownerId, false);

        assertEquals(Status.PUBLISHED, v.getStatus(), "Otros cambios no deben devolver a revisión");
    }

    @Test
    @DisplayName("Transiciones de estado: publish, archive y returnToReview")
    void stateTransitions() {
        Venture v = Venture.create("Arte UFPS", "Pinturas al óleo", VentureCategory.ARTE_Y_MANUALIDADES, ownerId, contact, serviceModes, null);
        assertEquals(Status.UNDER_REVIEW, v.getStatus());

        // Publicar fija fecha de publicación
        v.publish();
        assertEquals(Status.PUBLISHED, v.getStatus());
        assertNotNull(v.getPublishedAt());

        // returnToReview devuelve a UNDER_REVIEW
        v.returnToReview();
        assertEquals(Status.UNDER_REVIEW, v.getStatus());

        // Publicar nuevamente conserva la fecha original de publicación
        var firstPublishedAt = v.getPublishedAt();
        v.publish();
        assertEquals(Status.PUBLISHED, v.getStatus());
        assertEquals(firstPublishedAt, v.getPublishedAt());

        // Archivar desde publicado
        v.archive();
        assertEquals(Status.ARCHIVED, v.getStatus());

        // Desde ARCHIVED no se puede publicar ni archivar nuevamente
        assertThrows(DomainException.class, v::publish);
        assertThrows(DomainException.class, v::archive);
    }
}

