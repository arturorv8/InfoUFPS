package org.infoufps.api.academic;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.academic.PublicProfesorDetailDto;
import org.infoufps.application.academic.PublicProfesorListItemDto;
import org.infoufps.application.academic.PublicResenaDto;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.application.academic.GetProfesorDetailQuery;
import org.infoufps.application.academic.GetProfesoresPublicQuery;
import org.infoufps.application.academic.GetResenasByDestinoQuery;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.api.shared.security.ActorContext;
import org.infoufps.api.shared.security.CurrentAccount;
import org.infoufps.domain.account.AccountId;

@Path("/api/v1/public/profesores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfesorPublicController {

    @Inject
    GetProfesoresPublicQuery getProfesoresPublicQuery;

    @Inject
    GetProfesorDetailQuery getProfesorDetailQuery;

    @Inject
    GetResenasByDestinoQuery getResenasByDestinoQuery;

    @Inject
    ActorContext actorContext;

    @GET
    public Response list(
            @QueryParam("q") String query,
            @QueryParam("facultad") String facultadStr,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    ) {
        Facultad facultad = null;
        if (facultadStr != null && !facultadStr.isBlank()) {
            facultad = Facultad.fromString(facultadStr);
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<PublicProfesorListItemDto> result = getProfesoresPublicQuery.execute(query, facultad, pageRequest);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    public Response getDetail(@PathParam("id") String id) {
        CurrentAccount user = actorContext.currentAccount();
        AccountId requesterId = user.accountId() != null ? AccountId.of(user.accountId()) : null;
        boolean isAdmin = user.admin();
        PublicProfesorDetailDto detail = getProfesorDetailQuery.execute(id, requesterId, isAdmin);
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
        PageResult<PublicResenaDto> result = getResenasByDestinoQuery.getByProfesor(id, pageRequest);
        return Response.ok(result).build();
    }
}

