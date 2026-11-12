package org.infoufps.api.calendar;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.calendar.*;
import org.infoufps.application.calendar.GetModerationQueueQuery;
import org.infoufps.api.shared.security.ActorContext;
import org.infoufps.api.shared.security.CurrentAccount;
import org.infoufps.api.shared.params.ApiParams;

/**
 * Endpoints administrativos del calendario.
 */
@Path("/api/v1/admin/events")
@Produces(MediaType.APPLICATION_JSON)
public class AdminEventController {

    @Inject
    CreateEventHandler createHandler;

    @Inject
    UpdateEventHandler updateHandler;

    @Inject
    PublishEventHandler publishHandler;

    @Inject
    ArchiveEventHandler archiveHandler;

    @Inject
    GetModerationQueueQuery moderationQuery;

    @Inject
    ActorContext actorContext;

    /**
     * Crea un evento publicado. Solo administradores.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid EventRequest request) {
        String accountId = requireAdmin().requireAccountId();
        return Response.status(Response.Status.CREATED)
                .entity(createHandler.execute(new CreateEventCommand(request), accountId))
                .build();
    }

    /**
     * Corrige un evento. Solo administradores.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") String id,
                           @Valid EventRequest request) {
        String accountId = requireAdmin().requireAccountId();
        return Response.ok(updateHandler.execute(
                new UpdateEventCommand(id, accountId, true, request))).build();
    }

    /**
     * Cola de moderación por estado.
     */
    @GET
    public Response queue(@QueryParam("status") String status,
                          @QueryParam("page") String page,
                          @QueryParam("size") String size) {
        requireAdmin();
        return Response.ok(moderationQuery.execute(
                status,
                ApiParams.optionalInt(page, "page", 1),
                ApiParams.optionalInt(size, "size", 10))).build();
    }

    /**
     * Publica un evento en revisión.
     */
    @POST
    @Path("/{id}/publish")
    public Response publish(@PathParam("id") String id) {
        requireAdmin();
        return Response.ok(publishHandler.execute(new PublishEventCommand(id))).build();
    }

    /**
     * Archiva un evento.
     */
    @POST
    @Path("/{id}/archive")
    public Response archive(@PathParam("id") String id) {
        requireAdmin();
        return Response.ok(archiveHandler.execute(new ArchiveEventCommand(id))).build();
    }

    private CurrentAccount requireAdmin() {
        CurrentAccount user = actorContext.currentAccount();
        user.requireAdmin();
        return user;
    }
}
