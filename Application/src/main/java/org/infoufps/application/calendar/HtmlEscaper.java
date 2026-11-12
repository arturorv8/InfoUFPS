package org.infoufps.application.calendar;

/**
 * Utilidad para escapar texto que se inserta en HTML.
 * Se usa en la página de vista previa para compartir, ya que el título y el
 * lugar son escritos por usuarios.
 */
public final class HtmlEscaper {

    private HtmlEscaper() {
    }

    /**
     * Escapa caracteres especiales de HTML.
     *
     * @param value texto original.
     * @return texto seguro para insertar en HTML.
     */
    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
