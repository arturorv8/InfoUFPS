package org.infoufps.domain.venture;

import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.valueobjects.Email;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Objeto de Valor (Value Object) que modela el contacto principal de un emprendimiento.
 * Valida, normaliza y genera la URL directa de apertura según el tipo de canal.
 */
public final class Contact {

    private static final Pattern WHATSAPP_DIGITS_PATTERN = Pattern.compile("^3\\d{9}$");
    private static final Pattern INSTAGRAM_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._]{1,30}$");
    private static final int MAX_WEB_LENGTH = 512;

    private final ContactType type;
    private final String value;

    private Contact(ContactType type, String value) {
        this.type = Objects.requireNonNull(type, "El tipo de contacto es obligatorio");
        this.value = validateAndNormalize(type, value);
    }

    /**
     * Construye y valida una instancia de Contact.
     *
     * @param type  Tipo o canal de contacto.
     * @param value Valor textual ingresado para el contacto.
     * @return Contacto validado y normalizado.
     */
    public static Contact of(ContactType type, String value) {
        return new Contact(type, value);
    }

    private static String validateAndNormalize(ContactType type, String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El valor del contacto es obligatorio");
        }
        String trimmed = raw.strip();

        return switch (type) {
            case WHATSAPP -> normalizeWhatsApp(trimmed);
            case INSTAGRAM -> normalizeInstagram(trimmed);
            case EMAIL -> normalizeEmail(trimmed);
            case WEB -> normalizeWeb(trimmed);
        };
    }

    private static String normalizeWhatsApp(String raw) {
        // Remover espacios, guiones, paréntesis y signos más intermedios
        String cleaned = raw.replaceAll("[\\s\\-\\(\\)]", "");
        if (cleaned.startsWith("+57")) {
            cleaned = cleaned.substring(3);
        } else if (cleaned.startsWith("57") && cleaned.length() == 12) {
            cleaned = cleaned.substring(2);
        }
        if (!WHATSAPP_DIGITS_PATTERN.matcher(cleaned).matches()) {
            throw new DomainException("El número de WhatsApp debe ser un celular colombiano de 10 dígitos que empiece por 3");
        }
        return "+57" + cleaned;
    }

    private static String normalizeInstagram(String raw) {
        String clean = raw.strip();
        if (clean.startsWith("@")) {
            clean = clean.substring(1).strip();
        }
        if (!INSTAGRAM_USERNAME_PATTERN.matcher(clean).matches()) {
            throw new DomainException("El usuario de Instagram debe tener entre 1 y 30 caracteres alfanuméricos, puntos o guiones bajos, sin arroba");
        }
        return clean;
    }

    private static String normalizeEmail(String raw) {
        String normalized = Email.normalize(raw);
        if (normalized == null) {
            throw new DomainException("El formato del email de contacto es inválido: " + raw);
        }
        return normalized;
    }

    private static String normalizeWeb(String raw) {
        String clean = raw.strip();
        if (clean.length() > MAX_WEB_LENGTH) {
            throw new DomainException("La URL web no puede superar los " + MAX_WEB_LENGTH + " caracteres");
        }
        String lower = clean.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            throw new DomainException("La URL web de contacto debe iniciar con http:// o https://");
        }
        try {
            URI uri = new URI(clean);
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new DomainException("La URL web debe incluir un dominio válido");
            }
        } catch (Exception e) {
            throw new DomainException("La URL web es inválida: " + clean);
        }
        return clean;
    }

    /**
     * @return Canal de contacto.
     */
    public ContactType type() {
        return type;
    }

    /**
     * @return Valor normalizado almacenado en persistencia.
     */
    public String value() {
        return value;
    }

    /**
     * Genera una URL lista para abrir en el cliente o navegador a partir del valor validado y codificado.
     *
     * @return URL lista para ser consumida.
     */
    public String actionUrl() {
        return switch (type) {
            case WHATSAPP -> "https://wa.me/" + value.substring(1); // ej: https://wa.me/573001234567
            case INSTAGRAM -> "https://instagram.com/" + URLEncoder.encode(value, StandardCharsets.UTF_8);
            case EMAIL -> "mailto:" + value;
            case WEB -> value;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact contact)) return false;
        return type == contact.type && Objects.equals(value, contact.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return type + ":" + value;
    }
}

