package org.infoufps.application.shared;

public record CatalogoItemDto(
        String clave,
        String etiqueta,
        String descripcion
) {
    public static CatalogoItemDto of(String clave, String etiqueta, String descripcion) {
        return new CatalogoItemDto(clave, etiqueta, descripcion);
    }
}

