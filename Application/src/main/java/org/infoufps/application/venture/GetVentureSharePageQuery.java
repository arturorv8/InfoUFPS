package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.calendar.HtmlEscaper;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.venture.IVentureRepository;
import org.infoufps.domain.venture.Venture;
import org.infoufps.domain.venture.VentureId;

/**
 * Genera la página HTML con metadatos OpenGraph para compartir un emprendimiento en redes sociales
 * y redirigir al frontend. Escapa de forma segura el título y la descripción.
 */
@ApplicationScoped
public class GetVentureSharePageQuery {

    private final IVentureRepository ventureRepository;
    private final VentureSettings settings;

    @Inject
    public GetVentureSharePageQuery(IVentureRepository ventureRepository, VentureSettings settings) {
        this.ventureRepository = ventureRepository;
        this.settings = settings;
    }

    public VentureSharePageDto execute(String id) {
        Venture venture = ventureRepository.findById(VentureId.of(id))
                .orElseThrow(() -> new NotFoundException("Emprendimiento no encontrado: " + id));

        if (!venture.isVisible()) {
            throw new NotFoundException("Emprendimiento no encontrado o no disponible");
        }

        String rawUrl = settings.frontendBaseUrl() + settings.frontendVenturePath().replace("{id}", venture.getId().value());
        String ventureUrl = HtmlEscaper.escape(rawUrl);
        String logoUrl = HtmlEscaper.escape(settings.shareLogoUrl());
        String title = HtmlEscaper.escape(venture.getName());

        String rawDesc = venture.getDescription();
        if (rawDesc.length() > 200) {
            rawDesc = rawDesc.substring(0, 197) + "...";
        }
        String description = HtmlEscaper.escape(rawDesc);

        String html = "<!DOCTYPE html>\n"
                + "<html lang=\"es\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\">\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <title>" + title + " | InfoUFPS La Vitrina</title>\n"
                + "  <meta property=\"og:type\" content=\"website\">\n"
                + "  <meta property=\"og:title\" content=\"" + title + "\">\n"
                + "  <meta property=\"og:description\" content=\"" + description + "\">\n"
                + "  <meta property=\"og:image\" content=\"" + logoUrl + "\">\n"
                + "  <meta property=\"og:url\" content=\"" + ventureUrl + "\">\n"
                + "  <meta name=\"twitter:card\" content=\"summary_large_image\">\n"
                + "  <meta name=\"twitter:title\" content=\"" + title + "\">\n"
                + "  <meta name=\"twitter:description\" content=\"" + description + "\">\n"
                + "  <meta name=\"twitter:image\" content=\"" + logoUrl + "\">\n"
                + "  <meta http-equiv=\"refresh\" content=\"1;url=" + ventureUrl + "\">\n"
                + "</head>\n"
                + "<body>\n"
                + "  <main>\n"
                + "    <img src=\"" + logoUrl + "\" alt=\"InfoUFPS La Vitrina\" width=\"96\" height=\"96\">\n"
                + "    <h1>" + title + "</h1>\n"
                + "    <p>" + description + "</p>\n"
                + "    <p><a href=\"" + ventureUrl + "\">Ir al emprendimiento</a></p>\n"
                + "  </main>\n"
                + "</body>\n"
                + "</html>\n";

        return new VentureSharePageDto(html);
    }
}

