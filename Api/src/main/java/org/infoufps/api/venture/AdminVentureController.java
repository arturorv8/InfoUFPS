package org.infoufps.api.venture;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.api.shared.params.ApiParams;
import org.infoufps.api.shared.security.ActorContext;
import org.infoufps.api.venture.dto.ResolveReportRequest;
import org.infoufps.application.venture.AdminVentureDto;
import org.infoufps.application.venture.ArchiveVentureCommand;
import org.infoufps.application.venture.ArchiveVentureHandler;
import org.infoufps.application.venture.GetAdminVentureReportsQuery;
import org.infoufps.application.venture.GetAdminVenturesQuery;
import org.infoufps.application.venture.PublishVentureCommand;
import org.infoufps.application.venture.PublishVentureHandler;
import org.infoufps.application.venture.ResolveReportCommand;
import org.infoufps.application.venture.ResolveVentureReportHandler;
import org.infoufps.application.venture.VentureReportDto;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.venture.ReportStatus;

import java.util.Map;

/**
 * Endpoints de administración para la moderación de emprendimientos y reportes.
 */
@Path("/api/v1/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminVentureController {

    @Inject
    GetAdminVenturesQuery getAdminVenturesQuery;

    @Inject
    PublishVentureHandler publishVentureHandler;

    @Inject
    ArchiveVentureHandler archiveVentureHandler;

    @Inject
    GetAdminVentureReportsQuery getAdminVentureReportsQuery;

    @Inject
    ResolveVentureReportHandler resolveVentureReportHandler;

    @Inject
    ActorContext actorContext;

    /**
     * Consulta administrativa de emprendimientos con marcas de duplicado y contacto visible.
     */
    @GET
    @Path("/ventures")
    public Response getVentures(
            @QueryParam("status") String rawStatus,
            @QueryParam("page") String page,
            @QueryParam("size") String size) {

        actorContext.currentAccount().requireAdmin();

        Status status = parseStatus(rawStatus);
        PageRequest pageRequest = PageRequest.of(
                ApiParams.optionalInt(page, "page", 1),
                ApiParams.optionalInt(size, "size", 10)
        );

        PageResult<AdminVentureDto> result = getAdminVenturesQuery.execute(status, pageRequest);
        return Response.ok(result).build();
    }

    /**
     * Publica un emprendimiento en moderación.
     */
    @POST
    @Path("/ventures/{id}/publish")
    public Response publish(@PathParam("id") String id) {
        actorContext.currentAccount().requireAdmin();
        publishVentureHandler.execute(new PublishVentureCommand(id));
        return Response.ok(Map.of("id", id, "status", Status.PUBLISHED.name())).build();
    }

    /**
     * Archiva un emprendimiento.
     */
    @POST
    @Path("/ventures/{id}/archive")
    public Response archive(@PathParam("id") String id) {
        actorContext.currentAccount().requireAdmin();
        archiveVentureHandler.execute(new ArchiveVentureCommand(id));
        return Response.ok(Map.of("id", id, "status", Status.ARCHIVED.name())).build();
    }

    /**
     * Consulta administrativa de reportes de moderación.
     */
    @GET
    @Path("/venture-reports")
    public Response getReports(
            @QueryParam("status") String rawStatus,
            @QueryParam("page") String page,
            @QueryParam("size") String size) {

        actorContext.currentAccount().requireAdmin();

        ReportStatus status = parseReportStatus(rawStatus);
        PageRequest pageRequest = PageRequest.of(
                ApiParams.optionalInt(page, "page", 1),
                ApiParams.optionalInt(size, "size", 10)
        );

        PageResult<VentureReportDto> result = getAdminVentureReportsQuery.execute(status, pageRequest);
        return Response.ok(result).build();
    }

    /**
     * Resuelve un reporte de moderación eligiendo descartar o archivar.
     */
    @POST
    @Path("/venture-reports/{id}/resolve")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resolve(@PathParam("id") String id, @Valid ResolveReportRequest request) {
        String adminId = actorContext.currentAccount().requireAccountId();
        actorContext.currentAccount().requireAdmin();

        resolveVentureReportHandler.execute(new ResolveReportCommand(id, adminId, request.action()));
        return Response.ok(Map.of("id", id, "status", ReportStatus.RESOLVED.name())).build();
    }

    private Status parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Status.valueOf(value.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Estado de emprendimiento inválido: " + value);
        }
    }

    private ReportStatus parseReportStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ReportStatus.valueOf(value.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Estado de reporte inválido: " + value);
        }
    }
}

