package org.infoufps.api.academic;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.academic.ProfesorDetailDto;
import org.infoufps.application.academic.ProposeProfesorCommand;
import org.infoufps.application.academic.UpdateProposedProfesorCommand;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.application.academic.ProposeProfesorHandler;
import org.infoufps.application.academic.UpdateProposedProfesorHandler;
import org.infoufps.application.academic.GetProfesorPropuestasQuery;
import org.infoufps.api.shared.security.ActorContext;

import java.util.List;

@Path("/api/v1/user/profesores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserProfesorController {

    @Inject
    ProposeProfesorHandler proposeProfesorHandler;

    @Inject
    UpdateProposedProfesorHandler updateProposedProfesorHandler;

    @Inject
    GetProfesorPropuestasQuery getProfesorPropuestasQuery;

    @Inject
    ActorContext actorContext;

    public record ProposeRequest(
            @NotBlank @Size(max = 150) String nombre,
            String facultad,
            @Email @Size(max = 150) String email) {}

    public record UpdateProposalRequest(
            @NotBlank @Size(max = 150) String nombre,
            String facultad,
            @Email @Size(max = 150) String email) {}

    @POST
    public Response propose(@Valid ProposeRequest request) {
        String userId = actorContext.currentAccount().requireAccountId();
        Facultad facultad = (request.facultad() != null && !request.facultad().isBlank())
                ? Facultad.fromString(request.facultad())
                : Facultad.NO_ESPECIFICADA;

        ProposeProfesorCommand command = new ProposeProfesorCommand(
                request.nombre(),
                facultad,
                request.email(),
                userId
        );

        ProfesorDetailDto created = proposeProfesorHandler.execute(command);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/propuestas")
    public Response getMyProposals() {
        String userId = actorContext.currentAccount().requireAccountId();
        List<ProfesorDetailDto> proposals = getProfesorPropuestasQuery.execute(userId);
        return Response.ok(proposals).build();
    }

    @PUT
    @Path("/propuestas/{id}")
    public Response updateProposal(@PathParam("id") String id, @Valid UpdateProposalRequest request) {
        String userId = actorContext.currentAccount().requireAccountId();
        Facultad facultad = (request.facultad() != null && !request.facultad().isBlank())
                ? Facultad.fromString(request.facultad())
                : null;

        UpdateProposedProfesorCommand command = new UpdateProposedProfesorCommand(
                id,
                request.nombre(),
                facultad,
                request.email(),
                userId
        );

        ProfesorDetailDto updated = updateProposedProfesorHandler.execute(command);
        return Response.ok(updated).build();
    }
}

