package org.infoufps.application.calendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("HtmlEscaper: escape de contenido HTML")
class HtmlEscaperTest {

    @Test
    @DisplayName("Escapa los caracteres especiales de HTML")
    void escapesSpecialCharacters() {
        assertEquals("&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;",
                HtmlEscaper.escape("<script>alert(\"x\")</script>"));
        assertEquals("Tom &amp; Jerry", HtmlEscaper.escape("Tom & Jerry"));
        assertEquals("&#39;texto&#39;", HtmlEscaper.escape("'texto'"));
    }

    @Test
    @DisplayName("Un valor nulo se convierte en cadena vacía")
    void nullBecomesEmpty() {
        assertEquals("", HtmlEscaper.escape(null));
    }

    @Test
    @DisplayName("El texto sin caracteres especiales no cambia")
    void plainTextIsUnchanged() {
        assertEquals("Feria de ciencia", HtmlEscaper.escape("Feria de ciencia"));
    }
}
