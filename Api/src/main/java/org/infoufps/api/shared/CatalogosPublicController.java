package org.infoufps.api.shared;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.shared.GetCatalogosQuery;

@Path("/api/v1/public/catalogos")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogosPublicController {

    @Inject
    GetCatalogosQuery getCatalogosQuery;

    @GET
    @Path("/facultades")
    public Response getFacultades() {
        return Response.ok(getCatalogosQuery.getFacultades()).build();
    }

    @GET
    @Path("/tipos-materia")
    public Response getTiposMateria() {
        return Response.ok(getCatalogosQuery.getTiposMateria()).build();
    }

    @GET
    @Path("/creditos")
    public Response getCreditos() {
        return Response.ok(getCatalogosQuery.getCreditos()).build();
    }
}

