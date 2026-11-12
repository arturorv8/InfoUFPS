package org.infoufps.api.calendar;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.calendar.*;
import org.infoufps.application.calendar.GetMyFavoriteIdsQuery;
import org.infoufps.application.calendar.GetMyFavoritesQuery;
import org.infoufps.application.calendar.GetMyProposalsQuery;
import org.infoufps.api.shared.security.ActorContext;
import org.infoufps.api.shared.security.CurrentAccount;
import org.infoufps.api.shared.params.ApiParams;

/**
 * Endpoints del usuario autenticado.
 */
@Path("/api/v1/events")
@Produces(MediaType.APPLICATION_JSON)
public class UserEventController {

    @Inject
    ProposeEventHandler proposeHandler;

    @Inject
    UpdateEventHandler updateHandler;

    @Inject
    AddFavoriteHandler addFavoriteHandler;

    @Inject
    RemoveFavoriteHandler removeFavoriteHandler;

    @Inject
    GetMyProposalsQuery myProposalsQuery;

    @Inject
    GetMyFavoritesQuery myFavoritesQuery;

    @Inject
    GetMyFavoriteIdsQuery myFavoriteIdsQuery;

    @Inject
    ActorContext actorContext;

    /**
     * Propone un evento. Entra en revisión.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response propose(@Valid EventRequest request) {
        String accountId = actorContext.currentAccount().requireAccountId();
        return Response.status(Response.Status.CREATED)
                .entity(proposeHandler.execute(new ProposeEventCommand(accountId, request)))
                .build();
    }

    /**
     * Lista las propuestas de la cuenta autenticada.
     */
    @GET
    @Path("/mine")
    public Response mine(@QueryParam("page") String page,
                         @QueryParam("size") String size) {
        String accountId = actorContext.currentAccount().requireAccountId();
        return Response.ok(myProposalsQuery.execute(
                accountId,
                ApiParams.optionalInt(page, "page", 1),
                ApiParams.optionalInt(size, "size", 10))).build();
    }

    /**
     * Edita una propuesta propia mientras está en revisión.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") String id,
                           @Valid EventRequest request) {
        String accountId = actorContext.currentAccount().requireAccountId();
        return Response.ok(updateHandler.execute(
                new UpdateEventCommand(id, accountId, false, request))).build();
    }

    /**
     * Marca un evento publicado como favorito. Idempotente.
     */
    @PUT
    @Path("/{id}/favorite")
    public Response addFavorite(@PathParam("id") String id) {
        String accountId = actorContext.currentAccount().requireAccountId();
        addFavoriteHandler.execute(new AddFavoriteCommand(accountId, id));
        return Response.noContent().build();
    }

    /**
     * Quita un evento de favoritos. Idempotente.
     */
    @DELETE
    @Path("/{id}/favorite")
    public Response removeFavorite(@PathParam("id") String id) {
        String accountId = actorContext.currentAccount().requireAccountId();
        removeFavoriteHandler.execute(new RemoveFavoriteCommand(accountId, id));
        return Response.noContent().build();
    }

    /**
     * Lista los eventos publicados favoritos, ordenados por fecha del evento.
     */
    @GET
    @Path("/favorites")
    public Response favorites(@QueryParam("page") String page,
                              @QueryParam("size") String size) {
        String accountId = actorContext.currentAccount().requireAccountId();
        return Response.ok(myFavoritesQuery.execute(
                accountId,
                ApiParams.optionalInt(page, "page", 1),
                ApiParams.optionalInt(size, "size", 10))).build();
    }

    /**
     * Lista liviana de identificadores de eventos favoritos visibles.
     */
    @GET
    @Path("/favorites/ids")
    public Response favoriteIds() {
        String accountId = actorContext.currentAccount().requireAccountId();
        return Response.ok(myFavoriteIdsQuery.execute(accountId)).build();
    }
}
