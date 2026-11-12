package org.infoufps.api.academic;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.academic.MateriaDetailDto;
import org.infoufps.application.academic.AdminSaveMateriaCommand;
import org.infoufps.domain.academic.TipoMateria;
import org.infoufps.application.academic.AdminCreateMateriaHandler;
import org.infoufps.application.academic.AdminUpdateMateriaHandler;
import org.infoufps.api.shared.security.ActorContext;

@Path("/api/v1/admin/materias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminMateriaController {

    @Inject
    AdminCreateMateriaHandler adminCreateMateriaHandler;

    @Inject
    AdminUpdateMateriaHandler adminUpdateMateriaHandler;

    @Inject
    ActorContext actorContext;

    public record SaveMateriaRequest(
            @NotBlank @Size(max = 150) String nombre,
            @Size(max = 30) String codigo,
            @Min(1) Integer creditos,
            String tipo
    ) {}

    @POST
    public Response create(@Valid SaveMateriaRequest request) {
        actorContext.currentAccount().requireAdmin();
        TipoMateria tipo = (request.tipo() != null && !request.tipo().isBlank())
                ? TipoMateria.fromString(request.tipo())
                : null;

        AdminSaveMateriaCommand command = new AdminSaveMateriaCommand(
                request.nombre(),
                request.codigo(),
                request.creditos(),
                tipo
        );

        MateriaDetailDto created = adminCreateMateriaHandler.execute(command);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, @Valid SaveMateriaRequest request) {
        actorContext.currentAccount().requireAdmin();
        TipoMateria tipo = (request.tipo() != null && !request.tipo().isBlank())
                ? TipoMateria.fromString(request.tipo())
                : null;

        AdminSaveMateriaCommand command = new AdminSaveMateriaCommand(
                request.nombre(),
                request.codigo(),
                request.creditos(),
                tipo
        );

        MateriaDetailDto updated = adminUpdateMateriaHandler.execute(id, command);
        return Response.ok(updated).build();
    }
}

