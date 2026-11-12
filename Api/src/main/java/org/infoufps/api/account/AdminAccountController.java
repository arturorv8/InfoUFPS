package org.infoufps.api.account;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.api.shared.security.ActorContext;
import org.infoufps.api.shared.security.CurrentAccount;
import org.infoufps.application.account.AccountDto;
import org.infoufps.application.account.ActivateAccountHandler;
import org.infoufps.application.account.ChangeAccountRoleHandler;
import org.infoufps.application.account.SearchAccountsQuery;
import org.infoufps.application.account.SuspendAccountHandler;
import org.infoufps.domain.account.AccountSearchCriteria;
import org.infoufps.domain.account.AccountStatus;
import org.infoufps.domain.account.Role;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.util.Locale;

@Path("/api/v1/admin/accounts")
@Produces(MediaType.APPLICATION_JSON)
public class AdminAccountController {

    @Inject
    ActorContext actorContext;

    @Inject
    SearchAccountsQuery searchAccountsQuery;

    @Inject
    ChangeAccountRoleHandler changeAccountRoleHandler;

    @Inject
    SuspendAccountHandler suspendAccountHandler;

    @Inject
    ActivateAccountHandler activateAccountHandler;

    public record ChangeRoleRequest(
            @NotBlank String role
    ) {
    }

    @GET
    public Response search(@QueryParam("query") String query,
                           @QueryParam("role") String role,
                           @QueryParam("status") String status,
                           @QueryParam("page") Integer page,
                           @QueryParam("size") Integer size) {
        actorContext.currentAccount().requireAdmin();

        AccountSearchCriteria criteria = new AccountSearchCriteria(
                query,
                parseRole(role),
                parseStatus(status)
        );
        PageResult<AccountDto> result = searchAccountsQuery.execute(
                criteria, PageRequest.of(page, size));
        return Response.ok(result).build();
    }

    @PUT
    @Path("/{id}/role")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(@PathParam("id") String id, @Valid ChangeRoleRequest request) {
        CurrentAccount actor = actorContext.currentAccount();
        actor.requireAdmin();

        AccountDto updated = changeAccountRoleHandler.execute(
                id, parseRequiredRole(request.role()), actor.accountId());
        return Response.ok(updated).build();
    }

    @POST
    @Path("/{id}/suspend")
    public Response suspend(@PathParam("id") String id) {
        CurrentAccount actor = actorContext.currentAccount();
        actor.requireAdmin();

        return Response.ok(suspendAccountHandler.execute(id, actor.accountId())).build();
    }

    @POST
    @Path("/{id}/activate")
    public Response activate(@PathParam("id") String id) {
        actorContext.currentAccount().requireAdmin();
        return Response.ok(activateAccountHandler.execute(id)).build();
    }

    private Role parseRequiredRole(String value) {
        Role role = parseRole(value);
        if (role == null) {
            throw new DomainException("El rol es obligatorio");
        }
        return role;
    }

    private Role parseRole(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new DomainException("Rol inválido: " + value);
        }
    }

    private AccountStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return AccountStatus.valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new DomainException("Estado inválido: " + value);
        }
    }
}
