package org.infoufps.domain.venture;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Value Object Contact")
class ContactTest {

    @Test
    @DisplayName("WhatsApp: normaliza número colombiano y genera actionUrl")
    void whatsAppValid() {
        Contact c1 = Contact.of(ContactType.WHATSAPP, "3001234567");
        assertEquals("+573001234567", c1.value());
        assertEquals("https://wa.me/573001234567", c1.actionUrl());

        Contact c2 = Contact.of(ContactType.WHATSAPP, "+57 312 345 6789");
        assertEquals("+573123456789", c2.value());
        assertEquals("https://wa.me/573123456789", c2.actionUrl());

        Contact c3 = Contact.of(ContactType.WHATSAPP, "57-315-987-6543");
        assertEquals("+573159876543", c3.value());
        assertEquals("https://wa.me/573159876543", c3.actionUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2001234567", "300123456", "30012345678", "abcdefghij", "+13001234567", ""})
    @DisplayName("WhatsApp: rechaza formatos inválidos")
    void whatsAppInvalid(String invalid) {
        assertThrows(DomainException.class, () -> Contact.of(ContactType.WHATSAPP, invalid));
    }

    @Test
    @DisplayName("Instagram: remueve arroba, valida y genera actionUrl")
    void instagramValid() {
        Contact c1 = Contact.of(ContactType.INSTAGRAM, "@mi_tienda.ufps");
        assertEquals("mi_tienda.ufps", c1.value());
        assertEquals("https://instagram.com/mi_tienda.ufps", c1.actionUrl());

        Contact c2 = Contact.of(ContactType.INSTAGRAM, "tienda_oficial");
        assertEquals("tienda_oficial", c2.value());
        assertEquals("https://instagram.com/tienda_oficial", c2.actionUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "usuario con espacios", "usuario@invalido!", "este_usuario_supera_los_treinta_caracteres_permitidos"})
    @DisplayName("Instagram: rechaza usuarios inválidos o fuera de límite")
    void instagramInvalid(String invalid) {
        assertThrows(DomainException.class, () -> Contact.of(ContactType.INSTAGRAM, invalid));
    }

    @Test
    @DisplayName("Email: normaliza a minúsculas y genera actionUrl mailto")
    void emailValid() {
        Contact c = Contact.of(ContactType.EMAIL, "Contacto@EMPRENDIMIENTO.com");
        assertEquals("contacto@emprendimiento.com", c.value());
        assertEquals("mailto:contacto@emprendimiento.com", c.actionUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalido", "@dominio.com", "sin-arroba", "con espacios@correo.com", ""})
    @DisplayName("Email: rechaza emails mal formados")
    void emailInvalid(String invalid) {
        assertThrows(DomainException.class, () -> Contact.of(ContactType.EMAIL, invalid));
    }

    @Test
    @DisplayName("Web: valida http/https y genera actionUrl")
    void webValid() {
        Contact c1 = Contact.of(ContactType.WEB, "https://mitienda.com/catalogo");
        assertEquals("https://mitienda.com/catalogo", c1.value());
        assertEquals("https://mitienda.com/catalogo", c1.actionUrl());

        Contact c2 = Contact.of(ContactType.WEB, "http://sub.dominio.org/pagina");
        assertEquals("http://sub.dominio.org/pagina", c2.value());
        assertEquals("http://sub.dominio.org/pagina", c2.actionUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ftp://mitienda.com", "mitienda.com", "javascript:alert(1)", "https://", ""})
    @DisplayName("Web: rechaza protocolos no permitidos o URLs mal formadas")
    void webInvalid(String invalid) {
        assertThrows(DomainException.class, () -> Contact.of(ContactType.WEB, invalid));
    }

    @Test
    @DisplayName("Web: rechaza URLs que superen 512 caracteres")
    void webExceedsMaxLength() {
        String longUrl = "https://example.com/" + "a".repeat(500);
        assertThrows(DomainException.class, () -> Contact.of(ContactType.WEB, longUrl));
    }
}

