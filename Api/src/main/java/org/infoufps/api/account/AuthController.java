package org.infoufps.api.account;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.account.AccountDto;
import org.infoufps.application.account.AuthenticateAccountHandler;
import org.infoufps.application.account.LoginCommand;
import org.infoufps.application.account.LoginResult;
import org.infoufps.application.account.LogoutHandler;
import org.infoufps.application.account.RefreshSessionHandler;
import org.infoufps.application.account.RegisterAccountCommand;
import org.infoufps.application.account.RegisterAccountHandler;
import org.infoufps.domain.account.Account;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    RegisterAccountHandler registerAccountHandler;

    @Inject
    AuthenticateAccountHandler authenticateAccountHandler;

    @Inject
    RefreshSessionHandler refreshSessionHandler;

    @Inject
    LogoutHandler logoutHandler;

    public record RegisterRequest(
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 255) String name
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(max = 72) String password
    ) {
    }

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {
    }

    public record LogoutRequest(
            @NotBlank String refreshToken
    ) {
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        Account account = registerAccountHandler.execute(
                new RegisterAccountCommand(request.email(), request.password(), request.name()));
        return Response.status(Response.Status.CREATED)
                .entity(AccountDto.from(account))
                .build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        LoginResult result = authenticateAccountHandler.execute(
                new LoginCommand(request.email(), request.password()));
        return Response.ok(result).build();
    }

    @POST
    @Path("/refresh")
    public Response refresh(@Valid RefreshRequest request) {
        LoginResult result = refreshSessionHandler.execute(request.refreshToken());
        return Response.ok(result).build();
    }

    @POST
    @Path("/logout")
    public Response logout(@Valid LogoutRequest request) {
        logoutHandler.execute(request.refreshToken());
        return Response.noContent().build();
    }
}
