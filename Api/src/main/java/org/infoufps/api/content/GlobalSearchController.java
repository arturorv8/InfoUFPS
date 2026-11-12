package org.infoufps.api.content;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.content.GlobalSearchResultDto;
import org.infoufps.application.content.GlobalSearchQuery;

@Path("/api/v1/public/busqueda-global")
@Produces(MediaType.APPLICATION_JSON)
public class GlobalSearchController {

    @Inject
    GlobalSearchQuery globalSearchQuery;

    @GET
    public Response search(@QueryParam("q") String query) {
        GlobalSearchResultDto result = globalSearchQuery.execute(query);
        return Response.ok(result).build();
    }
}

