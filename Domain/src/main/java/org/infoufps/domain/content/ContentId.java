package org.infoufps.domain.content;

import org.infoufps.domain.shared.exceptions.DomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Objeto de Valor (Value Object) inmutable que actúa como el Identificador Único de un Content.
 * Encapsula la representación en texto de un UUID para garantizar que los identificadores de dominio
 * sean fuertemente tipados y cumplan con las reglas de no vacuidad.
 */
public final class ContentId {

    /** Valor textual interno que almacena la representación del UUID. */
    private final String value;

    /**
     * Constructor privado que valida e inmutabiliza la instancia.
     *
     * @param value Cadena de texto no nula ni vacía con el identificador.
     * @throws DomainException Si el valor es nulo o contiene únicamente espacios en blanco.
     */
    private ContentId(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El ContentID no puede estar vacío.");
        }
        this.value = value.strip();
    }

    /**
     * Genera de forma aleatoria un nuevo identificador único (UUID v4) envuelto en un ContentId.
     *
     * @return Una nueva instancia de ContentId con un identificador único generado.
     */
    public static ContentId generate() {
        return new ContentId(UUID.randomUUID().toString());
    }

    /**
     * Reconstruye o mapea un ContentId a partir de un valor textual existente (ej. proveniente de la BD).
     *
     * @param value Cadena con el identificador previamente guardado.
     * @return Instancia de ContentId asociada a dicho valor.
     */
    public static ContentId of(String value) {
        return new ContentId(value);
    }

    /**
     * Devuelve el valor textual crudo del identificador.
     *
     * @return Representación en formato String del ID.
     */
    public String value() {
        return value;
    }

    /**
     * Evalúa la igualdad estructural entre dos objetos ContentId basándose en el contenido de su valor.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentId that)) return false;
        return Objects.equals(value, that.value);
    }

    /**
     * Calcula el código hash basado exclusivamente en el valor único.
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Devuelve el valor textual directamente para simplificar la concatenación y logs.
     */
    @Override
    public String toString() {
        return value;
    }
}