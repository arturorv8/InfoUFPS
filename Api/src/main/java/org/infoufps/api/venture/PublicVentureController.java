package org.infoufps.api.venture;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.api.shared.params.ApiParams;
import org.infoufps.api.shared.security.ActorContext;
import org.infoufps.application.venture.GetVentureDetailQuery;
import org.infoufps.application.venture.GetVentureShareQuery;
import org.infoufps.application.venture.SearchVenturesQuery;
import org.infoufps.application.venture.VentureCardDto;
import org.infoufps.application.venture.VentureDetailDto;
import org.infoufps.application.venture.VentureShareDto;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.venture.ServiceMode;
import org.infoufps.domain.venture.VentureCategory;
import org.infoufps.domain.venture.VentureSearchCriteria;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Endpoints públicos del módulo Emprendimientos (La Vitrina).
 * Solo exponen emprendimientos activos y en estado PUBLISHED.
 */
@Path("/api/v1/public/ventures")
@Produces(MediaType.APPLICATION_JSON)
public class PublicVentureController {

    @Inject
    SearchVenturesQuery searchVenturesQuery;

    @Inject
    GetVentureDetailQuery getVentureDetailQuery;

    @Inject
    GetVentureShareQuery getVentureShareQuery;

    @Inject
    ActorContext actorContext;

    /**
     * Catálogo público de emprendimientos visibles con búsqueda, filtros y ordenamiento.
     */
    @GET
    public Response search(
            @QueryParam("q") String query,
            @QueryParam("categories") List<String> categories,
            @QueryParam("modes") List<String> modes,
            @QueryParam("sort") String sort,
            @QueryParam("page") String page,
            @QueryParam("size") String size) {

        Set<VentureCategory> categorySet = parseCategories(categories);
        Set<ServiceMode> modeSet = parseModes(modes);
        PageRequest pageRequest = PageRequest.of(
                ApiParams.optionalInt(page, "page", 1),
                ApiParams.optionalInt(size, "size", 10)
        );

        VentureSearchCriteria criteria = VentureSearchCriteria.of(
                query,
                categorySet,
                modeSet,
                sort,
                pageRequest
        );

        PageResult<VentureCardDto> result = searchVenturesQuery.execute(criteria);
        return Response.ok(result).build();
    }

    /**
     * Catálogo de categorías disponibles.
     */
    @GET
    @Path("/categories")
    public Response categories() {
        return Response.ok(Arrays.stream(VentureCategory.values()).map(Enum::name).toList()).build();
    }

    /**
     * Catálogo de modos de servicio disponibles.
     */
    @GET
    @Path("/modes")
    public Response modes() {
        return Response.ok(Arrays.stream(ServiceMode.values()).map(Enum::name).toList()).build();
    }

    /**
     * Detalle completo de un emprendimiento visible (con contacto, URL, promedio y total).
     */
    @GET
    @Path("/{id}")
    public Response detail(@PathParam("id") String id) {
        String currentAccountId = actorContext.currentAccount().accountId();
        VentureDetailDto detail = getVentureDetailQuery.execute(id, currentAccountId);
        return Response.ok(detail).build();
    }

    /**
     * Metadatos para compartir un emprendimiento visible en redes sociales.
     */
    @GET
    @Path("/{id}/share")
    public Response share(@PathParam("id") String id) {
        VentureShareDto share = getVentureShareQuery.execute(id);
        return Response.ok(share).build();
    }

    private Set<VentureCategory> parseCategories(List<String> rawCategories) {
        if (rawCategories == null || rawCategories.isEmpty()) {
            return Collections.emptySet();
        }
        Set<VentureCategory> result = new HashSet<>();
        for (String item : rawCategories) {
            if (item == null || item.isBlank()) {
                continue;
            }
            for (String part : item.split(",")) {
                String clean = part.strip();
                if (!clean.isEmpty()) {
                    result.add(VentureCategory.fromString(clean));
                }
            }
        }
        return result;
    }

    private Set<ServiceMode> parseModes(List<String> rawModes) {
        if (rawModes == null || rawModes.isEmpty()) {
            return Collections.emptySet();
        }
        Set<ServiceMode> result = new HashSet<>();
        for (String item : rawModes) {
            if (item == null || item.isBlank()) {
                continue;
            }
            for (String part : item.split(",")) {
                String clean = part.strip();
                if (!clean.isEmpty()) {
                    result.add(ServiceMode.fromString(clean));
                }
            }
        }
        return result;
    }
}

