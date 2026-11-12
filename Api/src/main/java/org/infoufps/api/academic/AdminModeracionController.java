package org.infoufps.api.academic;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.academic.MateriaDetailDto;
import org.infoufps.application.academic.ModeracionWarningDto;
import org.infoufps.application.academic.ProfesorDetailDto;
import org.infoufps.application.academic.ModerateEntityCommand;
import org.infoufps.application.academic.ResenaDto;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.academic.TipoDestino;
import org.infoufps.domain.academic.TipoMateria;
import org.infoufps.application.academic.ModerateMateriaHandler;
import org.infoufps.application.academic.ModerateProfesorHandler;
import org.infoufps.application.academic.ModerateResenaHandler;
import org.infoufps.application.academic.GetModeracionMateriasQuery;
import org.infoufps.application.academic.GetModeracionProfesoresQuery;
import org.infoufps.application.academic.GetModeracionResenasQuery;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.api.shared.security.ActorContext;
import org.infoufps.application.academic.SimilarityService;

import java.util.List;

@Path("/api/v1/admin/moderacion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminModeracionController {

    @Inject
    GetModeracionProfesoresQuery getModeracionProfesoresQuery;

    @Inject
    GetModeracionMateriasQuery getModeracionMateriasQuery;

    @Inject
    GetModeracionResenasQuery getModeracionResenasQuery;

    @Inject
    ModerateProfesorHandler moderateProfesorHandler;

    @Inject
    ModerateMateriaHandler moderateMateriaHandler;

    @Inject
    ModerateResenaHandler moderateResenaHandler;

    @Inject
    SimilarityService similarityService;

    @Inject
    ActorContext actorContext;

    public record CorrectAndPublishProfesorRequest(
            @Size(max = 150) String nuevoNombre,
            String nuevaFacultad,
            @Email @Size(max = 150) String nuevoEmail
    ) {}

    public record CorrectAndPublishMateriaRequest(
            @Size(max = 150) String nuevoNombre,
            @Size(max = 30) String nuevoCodigo,
            @Min(1) Integer nuevosCreditos,
            String nuevoTipo
    ) {}

    // -- Colas de Profesores ----------------------------------------------

    @GET
    @Path("/profesores")
    public Response getProfesoresQueue(
            @QueryParam("estado") String estadoStr,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    ) {
        actorContext.currentAccount().requireAdmin();
        EstadoModeracion estado = (estadoStr != null && !estadoStr.isBlank())
                ? EstadoModeracion.fromString(estadoStr)
                : EstadoModeracion.EN_REVISION;

        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<ProfesorDetailDto> result = getModeracionProfesoresQuery.execute(estado, pageRequest);
        return Response.ok(result).build();
    }

    @PUT
    @Path("/profesores/{id}/publicar")
    public Response publishProfesor(@PathParam("id") String id, @Valid CorrectAndPublishProfesorRequest request) {
        actorContext.currentAccount().requireAdmin();
        ModerateEntityCommand command = null;
        if (request != null) {
            Facultad fac = (request.nuevaFacultad() != null && !request.nuevaFacultad().isBlank())
                    ? Facultad.fromString(request.nuevaFacultad())
                    : null;
            command = new ModerateEntityCommand(request.nuevoNombre(), fac, request.nuevoEmail(), null, null, null);
        }
        ProfesorDetailDto updated = moderateProfesorHandler.publish(id, command);
        return Response.ok(updated).build();
    }

    @PUT
    @Path("/profesores/{id}/archivar")
    public Response archiveProfesor(@PathParam("id") String id) {
        actorContext.currentAccount().requireAdmin();
        ProfesorDetailDto updated = moderateProfesorHandler.archive(id);
        return Response.ok(updated).build();
    }

    @GET
    @Path("/profesores/similares")
    public Response checkProfesorSimilarity(@QueryParam("nombre") String nombre, @QueryParam("facultad") String facultadStr) {
        actorContext.currentAccount().requireAdmin();
        Facultad fac = (facultadStr != null && !facultadStr.isBlank()) ? Facultad.fromString(facultadStr) : null;
        List<ModeracionWarningDto> warnings = similarityService.checkProfesorSimilarity(nombre, fac);
        return Response.ok(warnings).build();
    }

    // -- Colas de Materias ------------------------------------------------

    @GET
    @Path("/materias")
    public Response getMateriasQueue(
            @QueryParam("estado") String estadoStr,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    ) {
        actorContext.currentAccount().requireAdmin();
        EstadoModeracion estado = (estadoStr != null && !estadoStr.isBlank())
                ? EstadoModeracion.fromString(estadoStr)
                : EstadoModeracion.EN_REVISION;

        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<MateriaDetailDto> result = getModeracionMateriasQuery.execute(estado, pageRequest);
        return Response.ok(result).build();
    }

    @PUT
    @Path("/materias/{id}/publicar")
    public Response publishMateria(@PathParam("id") String id, @Valid CorrectAndPublishMateriaRequest request) {
        actorContext.currentAccount().requireAdmin();
        ModerateEntityCommand command = null;
        if (request != null) {
            TipoMateria tipo = (request.nuevoTipo() != null && !request.nuevoTipo().isBlank())
                    ? TipoMateria.fromString(request.nuevoTipo())
                    : null;
            command = new ModerateEntityCommand(
                    request.nuevoNombre(),
                    null,
                    null,
                    request.nuevoCodigo(),
                    request.nuevosCreditos(),
                    tipo
            );
        }
        MateriaDetailDto updated = moderateMateriaHandler.publish(id, command);
        return Response.ok(updated).build();
    }

    @PUT
    @Path("/materias/{id}/archivar")
    public Response archiveMateria(@PathParam("id") String id) {
        actorContext.currentAccount().requireAdmin();
        MateriaDetailDto updated = moderateMateriaHandler.archive(id);
        return Response.ok(updated).build();
    }

    @GET
    @Path("/materias/similares")
    public Response checkMateriaSimilarity(@QueryParam("nombre") String nombre) {
        actorContext.currentAccount().requireAdmin();
        List<ModeracionWarningDto> warnings = similarityService.checkMateriaSimilarity(nombre);
        return Response.ok(warnings).build();
    }

    // -- Colas de Reseñas -------------------------------------------------

    @GET
    @Path("/resenas")
    public Response getResenasQueue(
            @QueryParam("destino") String destinoStr,
            @QueryParam("estado") String estadoStr,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    ) {
        actorContext.currentAccount().requireAdmin();
        TipoDestino destino = (destinoStr != null && !destinoStr.isBlank())
                ? TipoDestino.fromString(destinoStr)
                : null;
        EstadoModeracion estado = (estadoStr != null && !estadoStr.isBlank())
                ? EstadoModeracion.fromString(estadoStr)
                : EstadoModeracion.EN_REVISION;

        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<ResenaDto> result = getModeracionResenasQuery.execute(destino, estado, pageRequest);
        return Response.ok(result).build();
    }

    @PUT
    @Path("/resenas/{id}/publicar")
    public Response publishResena(@PathParam("id") String id) {
        actorContext.currentAccount().requireAdmin();
        ResenaDto updated = moderateResenaHandler.publish(id);
        return Response.ok(updated).build();
    }

    @PUT
    @Path("/resenas/{id}/archivar")
    public Response archiveResena(@PathParam("id") String id) {
        actorContext.currentAccount().requireAdmin();
        ResenaDto updated = moderateResenaHandler.archive(id);
        return Response.ok(updated).build();
    }
}

