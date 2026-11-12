package org.infoufps.api.account;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.api.shared.security.ActorContext;
import org.infoufps.application.account.ChangePasswordCommand;
import org.infoufps.application.account.ChangePasswordHandler;
import org.infoufps.application.account.GetCurrentAccountQuery;
import org.infoufps.application.account.UpdateAccountNameHandler;

@Path("/api/v1/me")
@Produces(MediaType.APPLICATION_JSON)
public class MeController {

    @Inject
    ActorContext actorContext;

    @Inject
    GetCurrentAccountQuery getCurrentAccountQuery;

    @Inject
    UpdateAccountNameHandler updateAccountNameHandler;

    @Inject
    ChangePasswordHandler changePasswordHandler;

    public record UpdateNameRequest(
            @NotBlank @Size(max = 255) String name
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 72) String newPassword
    ) {
    }

    @GET
    public Response me() {
        String accountId = actorContext.currentAccount().requireAccountId();
        return Response.ok(getCurrentAccountQuery.execute(accountId)).build();
    }

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateName(@Valid UpdateNameRequest request) {
        String accountId = actorContext.currentAccount().requireAccountId();
        return Response.ok(updateAccountNameHandler.execute(accountId, request.name())).build();
    }

    @PUT
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(@Valid ChangePasswordRequest request) {
        String accountId = actorContext.currentAccount().requireAccountId();
        changePasswordHandler.execute(accountId,
                new ChangePasswordCommand(request.currentPassword(), request.newPassword()));
        return Response.noContent().build();
    }
}
