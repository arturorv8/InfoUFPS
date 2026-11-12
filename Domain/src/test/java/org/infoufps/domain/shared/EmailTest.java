package org.infoufps.domain.shared;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.valueobjects.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Email: validación y normalización de correos")
class EmailTest {

    @Test
    @DisplayName("Email válido con TLD largo (> 7 caracteres)")
    void testLongTldEmailIsValid() {
        Email email1 = Email.of("docente@ufps.education");
        assertEquals("docente@ufps.education", email1.value());

        Email email2 = Email.of("contacto@universidad.technology");
        assertEquals("contacto@universidad.technology", email2.value());
    }

    @Test
    @DisplayName("Email normaliza a minúsculas y elimina espacios")
    void testEmailTrimmingAndLowercasing() {
        Email email = Email.of("  Docente.Prueba@UFPS.EDU.CO  ");
        assertEquals("docente.prueba@ufps.edu.co", email.value());
    }

    @Test
    @DisplayName("Email inválido lanza DomainException y vacío retorna null")
    void testInvalidEmailThrows() {
        assertThrows(DomainException.class, () -> Email.of("not-an-email"));
        assertThrows(DomainException.class, () -> Email.of("@ufps.edu.co"));
        assertThrows(DomainException.class, () -> Email.of("usuario@"));
        assertNull(Email.of(null));
        assertNull(Email.of("   "));
    }
}
