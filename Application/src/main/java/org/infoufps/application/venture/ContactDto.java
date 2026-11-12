package org.infoufps.application.venture;

import org.infoufps.domain.venture.Contact;
import org.infoufps.domain.venture.ContactType;

/**
 * DTO que representa el canal de contacto y su URL directa de consumo.
 */
public record ContactDto(
        ContactType type,
        String value,
        String actionUrl
) {
    public static ContactDto from(Contact contact) {
        if (contact == null) {
            return null;
        }
        return new ContactDto(
                contact.type(),
                contact.value(),
                contact.actionUrl()
        );
    }
}

