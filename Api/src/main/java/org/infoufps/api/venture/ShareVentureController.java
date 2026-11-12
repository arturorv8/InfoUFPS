package org.infoufps.api.venture;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.venture.GetVentureSharePageQuery;
import org.infoufps.application.venture.VentureSharePageDto;

/**
 * Página HTML con etiquetas de vista previa OpenGraph para compartir un emprendimiento en redes sociales.
 */
@Path("/share/ventures")
public class ShareVentureController {

    @Inject
    GetVentureSharePageQuery sharePageQuery;

    /**
     * Devuelve la página de vista previa y redirige a la vista del emprendimiento en el frontend.
     */
    @GET
    @Path("/{id}")
    @Produces("text/html; charset=utf-8")
    public Response page(@PathParam("id") String id) {
        VentureSharePageDto page = sharePageQuery.execute(id);
        return Response.ok(page.html())
                .type("text/html; charset=utf-8")
                .build();
    }
}

