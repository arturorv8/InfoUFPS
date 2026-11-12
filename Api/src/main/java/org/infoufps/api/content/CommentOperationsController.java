package org.infoufps.api.content;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.api.shared.security.ActorContext;
import org.infoufps.application.content.CommentDto;
import org.infoufps.application.content.DeleteCommentCommand;
import org.infoufps.application.content.DeleteCommentHandler;
import org.infoufps.application.content.UpdateCommentCommand;
import org.infoufps.application.content.UpdateCommentHandler;

/**
 * Operaciones genéricas sobre comentarios individuales: edición y borrado lógico.
 * Solo el autor del comentario o un administrador tienen permiso.
 */
@Path("/api/v1/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommentOperationsController {

    @Inject
    UpdateCommentHandler updateCommentHandler;

    @Inject
    DeleteCommentHandler deleteCommentHandler;

    @Inject
    ActorContext actorContext;

    public record UpdateCommentRequest(
            @NotBlank @Size(max = 1000) String text
    ) {
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id,
                           @Valid UpdateCommentRequest request) {
        String actorAccountId = actorContext.currentAccount().requireAccountId();
        boolean isAdmin = actorContext.currentAccount().admin();

        CommentDto updated = updateCommentHandler.execute(
                new UpdateCommentCommand(id, actorAccountId, isAdmin, request.text()));
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        String actorAccountId = actorContext.currentAccount().requireAccountId();
        boolean isAdmin = actorContext.currentAccount().admin();

        deleteCommentHandler.execute(
                new DeleteCommentCommand(id, actorAccountId, isAdmin));
        return Response.noContent().build();
    }
}

