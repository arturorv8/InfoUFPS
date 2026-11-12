package org.infoufps.api.academic;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.academic.PublicMateriaDetailDto;
import org.infoufps.application.academic.PublicMateriaListItemDto;
import org.infoufps.application.academic.PublicResenaDto;
import org.infoufps.domain.academic.TipoMateria;
import org.infoufps.application.academic.GetMateriaDetailQuery;
import org.infoufps.application.academic.GetMateriasPublicQuery;
import org.infoufps.application.academic.GetResenasByDestinoQuery;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.api.shared.security.ActorContext;
import org.infoufps.api.shared.security.CurrentAccount;
import org.infoufps.domain.account.AccountId;

@Path("/api/v1/public/materias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MateriaPublicController {

    @Inject
    GetMateriasPublicQuery getMateriasPublicQuery;

    @Inject
    GetMateriaDetailQuery getMateriaDetailQuery;

    @Inject
    GetResenasByDestinoQuery getResenasByDestinoQuery;

    @Inject
    ActorContext actorContext;

    @GET
    public Response list(
            @QueryParam("q") String query,
            @QueryParam("creditos") Integer creditos,
            @QueryParam("tipo") String tipoStr,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    ) {
        TipoMateria tipo = null;
        if (tipoStr != null && !tipoStr.isBlank()) {
            tipo = TipoMateria.fromString(tipoStr);
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<PublicMateriaListItemDto> result = getMateriasPublicQuery.execute(query, creditos, tipo, pageRequest);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    public Response getDetail(@PathParam("id") String id) {
        CurrentAccount user = actorContext.currentAccount();
        AccountId requesterId = user.accountId() != null ? AccountId.of(user.accountId()) : null;
        boolean isAdmin = user.admin();
        PublicMateriaDetailDto detail = getMateriaDetailQuery.execute(id, requesterId, isAdmin);
        return Response.ok(detail).build();
    }

    @GET
    @Path("/{id}/resenas")
    public Response getResenas(
            @PathParam("id") String id,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<PublicResenaDto> result = getResenasByDestinoQuery.getByMateria(id, pageRequest);
        return Response.ok(result).build();
    }
}

