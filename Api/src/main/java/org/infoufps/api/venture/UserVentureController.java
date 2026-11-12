package org.infoufps.api.venture;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.api.shared.params.ApiParams;
import org.infoufps.api.shared.security.ActorContext;
import org.infoufps.api.shared.security.CurrentAccount;
import org.infoufps.api.venture.dto.CreateVentureRequest;
import org.infoufps.api.venture.dto.RateVentureRequest;
import org.infoufps.api.venture.dto.ReportVentureRequest;
import org.infoufps.api.venture.dto.UpdateVentureRequest;
import org.infoufps.application.venture.ActivateVentureCommand;
import org.infoufps.application.venture.ActivateVentureHandler;
import org.infoufps.application.venture.AddFavoriteCommand;
import org.infoufps.application.venture.AddFavoriteHandler;
import org.infoufps.application.venture.CreateVentureCommand;
import org.infoufps.application.venture.CreateVentureHandler;
import org.infoufps.application.venture.DeactivateVentureCommand;
import org.infoufps.application.venture.DeactivateVentureHandler;
import org.infoufps.application.venture.DeleteRatingCommand;
import org.infoufps.application.venture.DeleteRatingHandler;
import org.infoufps.application.venture.GetMyFavoriteIdsQuery;
import org.infoufps.application.venture.GetMyFavoritesQuery;
import org.infoufps.application.venture.GetMyVentureRatingQuery;
import org.infoufps.application.venture.GetMyVenturesQuery;
import org.infoufps.application.venture.RateVentureCommand;
import org.infoufps.application.venture.RateVentureHandler;
import org.infoufps.application.venture.RemoveFavoriteCommand;
import org.infoufps.application.venture.RemoveFavoriteHandler;
import org.infoufps.application.venture.ReportVentureCommand;
import org.infoufps.application.venture.ReportVentureHandler;
import org.infoufps.application.venture.UpdateVentureCommand;
import org.infoufps.application.venture.UpdateVentureHandler;
import org.infoufps.application.venture.VentureCardDto;
import org.infoufps.application.venture.VentureDetailDto;
import org.infoufps.application.venture.VentureRatingDto;
import org.infoufps.application.venture.VentureReportDto;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.util.List;
import java.util.Map;

/**
 * Endpoints para operaciones de usuarios autenticados sobre emprendimientos.
 */
@Path("/api/v1/ventures")
@Produces(MediaType.APPLICATION_JSON)
public class UserVentureController {

    @Inject
    CreateVentureHandler createVentureHandler;

    @Inject
    UpdateVentureHandler updateVentureHandler;

    @Inject
    ActivateVentureHandler activateVentureHandler;

    @Inject
    DeactivateVentureHandler deactivateVentureHandler;

    @Inject
    RateVentureHandler rateVentureHandler;

    @Inject
    DeleteRatingHandler deleteRatingHandler;

    @Inject
    GetMyVentureRatingQuery getMyVentureRatingQuery;

    @Inject
    AddFavoriteHandler addFavoriteHandler;

    @Inject
    RemoveFavoriteHandler removeFavoriteHandler;

    @Inject
    GetMyVenturesQuery getMyVenturesQuery;

    @Inject
    GetMyFavoritesQuery getMyFavoritesQuery;

    @Inject
    GetMyFavoriteIdsQuery getMyFavoriteIdsQuery;

    @Inject
    ReportVentureHandler reportVentureHandler;

    @Inject
    ActorContext actorContext;

    /**
     * Registra un nuevo emprendimiento para la cuenta autenticada.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid CreateVentureRequest request) {
        String accountId = actorContext.currentAccount().requireAccountId();
        CreateVentureCommand command = new CreateVentureCommand(
                request.name(),
                request.description(),
                request.category(),
                request.contactType(),
                request.contactValue(),
                request.serviceModes(),
                request.location(),
                accountId
        );
        VentureDetailDto result = createVentureHandler.execute(command);
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    /**
     * Lista los emprendimientos propios registrados por la cuenta autenticada.
     */
    @GET
    @Path("/mine")
    public Response mine() {
        String accountId = actorContext.currentAccount().requireAccountId();
        List<VentureDetailDto> mine = getMyVenturesQuery.execute(accountId);
        return Response.ok(mine).build();
    }

    /**
     * Consulta paginada de emprendimientos favoritos del usuario.
     */
    @GET
    @Path("/favorites")
    public Response favorites(@QueryParam("page") String page,
                              @QueryParam("size") String size) {
        String accountId = actorContext.currentAccount().requireAccountId();
        PageRequest pageRequest = PageRequest.of(
                ApiParams.optionalInt(page, "page", 1),
                ApiParams.optionalInt(size, "size", 10)
        );
        PageResult<VentureCardDto> result = getMyFavoritesQuery.execute(accountId, pageRequest);
        return Response.ok(result).build();
    }

    /**
     * Lista de identificadores de emprendimientos marcados como favoritos.
     */
    @GET
    @Path("/favorites/ids")
    public Response favoriteIds() {
        String accountId = actorContext.currentAccount().requireAccountId();
        List<String> ids = getMyFavoriteIdsQuery.execute(accountId);
        return Response.ok(ids).build();
    }

    /**
     * Edita un emprendimiento propio (o de cualquier cuenta si es administrador).
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") String id, @Valid UpdateVentureRequest request) {
        CurrentAccount actor = actorContext.currentAccount();
        String accountId = actor.requireAccountId();
        UpdateVentureCommand command = new UpdateVentureCommand(
                id,
                request.name(),
                request.description(),
                request.category(),
                request.contactType(),
                request.contactValue(),
                request.serviceModes(),
                request.location(),
                accountId,
                actor.admin()
        );
        VentureDetailDto updated = updateVentureHandler.execute(command);
        return Response.ok(updated).build();
    }

    /**
     * Activa un emprendimiento propio (o como administrador).
     */
    @POST
    @Path("/{id}/activate")
    public Response activate(@PathParam("id") String id) {
        CurrentAccount actor = actorContext.currentAccount();
        String accountId = actor.requireAccountId();
        activateVentureHandler.execute(new ActivateVentureCommand(id, accountId, actor.admin()));
        return Response.ok(Map.of("id", id, "active", true)).build();
    }

    /**
     * Desactiva un emprendimiento propio (o como administrador).
     */
    @POST
    @Path("/{id}/deactivate")
    public Response deactivate(@PathParam("id") String id) {
        CurrentAccount actor = actorContext.currentAccount();
        String accountId = actor.requireAccountId();
        deactivateVentureHandler.execute(new DeactivateVentureCommand(id, accountId, actor.admin()));
        return Response.ok(Map.of("id", id, "active", false)).build();
    }

    /**
     * Califica un emprendimiento publicado con un puntaje de 1 a 5 estrellas.
     * Requiere login y correo electrónico verificado.
     */
    @PUT
    @Path("/{id}/rating")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response rate(@PathParam("id") String id, @Valid RateVentureRequest request) {
        CurrentAccount actor = actorContext.currentAccount();
        actor.requireEmailVerified();
        VentureRatingDto rating = rateVentureHandler.execute(
                new RateVentureCommand(id, actor.requireAccountId(), request.score())
        );
        return Response.ok(rating).build();
    }

    /**
     * Elimina la calificación otorgada por el usuario a un emprendimiento.
     */
    @DELETE
    @Path("/{id}/rating")
    public Response deleteRating(@PathParam("id") String id) {
        String accountId = actorContext.currentAccount().requireAccountId();
        deleteRatingHandler.execute(new DeleteRatingCommand(id, accountId));
        return Response.noContent().build();
    }

    /**
     * Obtiene la calificación otorgada por el usuario autenticado a un emprendimiento.
     */
    @GET
    @Path("/{id}/rating")
    public Response getMyRating(@PathParam("id") String id) {
        String accountId = actorContext.currentAccount().requireAccountId();
        return getMyVentureRatingQuery.execute(accountId, id)
                .map(rating -> Response.ok(rating).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    /**
     * Agrega un emprendimiento publicado a favoritos. Idempotente.
     */
    @PUT
    @Path("/{id}/favorite")
    public Response addFavorite(@PathParam("id") String id) {
        String accountId = actorContext.currentAccount().requireAccountId();
        addFavoriteHandler.execute(new AddFavoriteCommand(id, accountId));
        return Response.noContent().build();
    }

    /**
     * Quita un emprendimiento de favoritos. Idempotente.
     */
    @DELETE
    @Path("/{id}/favorite")
    public Response removeFavorite(@PathParam("id") String id) {
        String accountId = actorContext.currentAccount().requireAccountId();
        removeFavoriteHandler.execute(new RemoveFavoriteCommand(id, accountId));
        return Response.noContent().build();
    }

    /**
     * Reporta un emprendimiento para moderación.
     */
    @POST
    @Path("/{id}/report")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response report(@PathParam("id") String id, @Valid ReportVentureRequest request) {
        String accountId = actorContext.currentAccount().requireAccountId();
        VentureReportDto report = reportVentureHandler.execute(
                new ReportVentureCommand(id, accountId, request.reason(), request.comment())
        );
        return Response.status(Response.Status.CREATED).entity(report).build();
    }
}
