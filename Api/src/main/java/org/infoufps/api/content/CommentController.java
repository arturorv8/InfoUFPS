package org.infoufps.api.content;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.api.shared.security.ActorContext;
import org.infoufps.application.content.CommentDto;
import org.infoufps.application.content.CreateCommentCommand;
import org.infoufps.application.content.CreateCommentHandler;
import org.infoufps.application.content.GetCommentsByContentQuery;
import org.infoufps.domain.shared.pagination.PageRequest;

/**
 * Comentarios de un contenido. Mantiene el contrato de API previo para la ruta
 * /api/v1/contents/{contentId}/comments con soporte adicional de paginación.
 */
@Path("/api/v1/contents/{contentId}/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommentController {

    @Inject
    GetCommentsByContentQuery getCommentsByContentQuery;

    @Inject
    CreateCommentHandler createCommentHandler;

    @Inject
    ActorContext actorContext;

    public record CreateCommentRequest(
            @NotBlank @Size(max = 1000) String text
    ) {
    }

    @GET
    public Response list(@PathParam("contentId") String contentId,
                         @QueryParam("page") Integer page,
                         @QueryParam("size") Integer size) {
        if (page != null || size != null) {
            return Response.ok(getCommentsByContentQuery.execute(contentId, PageRequest.of(page, size))).build();
        }
        return Response.ok(getCommentsByContentQuery.execute(contentId)).build();
    }

    @POST
    public Response create(@PathParam("contentId") String contentId,
                           @Valid CreateCommentRequest request) {
        String authorId = actorContext.currentAccount().requireAccountId();
        CommentDto created = createCommentHandler.execute(
                new CreateCommentCommand(contentId, authorId, request.text()));
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}
