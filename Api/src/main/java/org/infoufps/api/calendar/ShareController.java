package org.infoufps.api.calendar;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.calendar.SharePageDto;
import org.infoufps.application.calendar.GetSharePageQuery;

/**
 * Página HTML con etiquetas de vista previa para compartir un evento.
 */
@Path("/share/events")
public class ShareController {

    @Inject
    GetSharePageQuery sharePageQuery;

    /**
     * Devuelve la página de vista previa y redirige al evento del frontend.
     */
    @GET
    @Path("/{id}")
    @Produces("text/html; charset=utf-8")
    public Response page(@PathParam("id") String id) {
        SharePageDto page = sharePageQuery.execute(id);
        return Response.ok(page.html())
                .type("text/html; charset=utf-8")
                .build();
    }
}
