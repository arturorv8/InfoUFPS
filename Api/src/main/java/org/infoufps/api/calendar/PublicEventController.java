package org.infoufps.api.calendar;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoufps.application.calendar.CalendarFileDto;
import org.infoufps.application.calendar.*;
import org.infoufps.api.shared.params.ApiParams;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints públicos del calendario. Solo exponen eventos publicados.
 */
@Path("/api/v1/public/events")
@Produces(MediaType.APPLICATION_JSON)
public class PublicEventController {

    @Inject
    GetEventSummaryQuery summaryQuery;

    @Inject
    GetEventsQuery eventsQuery;

    @Inject
    GetUpcomingEventsQuery upcomingQuery;

    @Inject
    GetEventDetailQuery detailQuery;

    @Inject
    GetEventTypesQuery typesQuery;

    @Inject
    GetShareEventQuery shareQuery;

    @Inject
    GetEventCalendarQuery calendarQuery;

    /**
     * Resumen de eventos publicados por día.
     */
    @GET
    @Path("/summary")
    public Response summary(@QueryParam("from") String from,
                            @QueryParam("to") String to,
                            @QueryParam("types") List<String> types) {
        LocalDate fromDate = ApiParams.requiredDate(from, "from");
        LocalDate toDate = ApiParams.requiredDate(to, "to");
        return Response.ok(summaryQuery.execute(fromDate, toDate, types)).build();
    }

    /**
     * Eventos publicados que se solapan con el rango solicitado.
     */
    @GET
    public Response events(@QueryParam("from") String from,
                           @QueryParam("to") String to,
                           @QueryParam("types") List<String> types) {
        LocalDate fromDate = ApiParams.requiredDate(from, "from");
        LocalDate toDate = ApiParams.requiredDate(to, "to");
        return Response.ok(eventsQuery.execute(fromDate, toDate, types)).build();
    }

    /**
     * Próximos eventos y eventos en curso, paginados.
     */
    @GET
    @Path("/upcoming")
    public Response upcoming(@QueryParam("types") List<String> types,
                             @QueryParam("page") String page,
                             @QueryParam("size") String size) {
        return Response.ok(upcomingQuery.execute(
                types,
                ApiParams.optionalInt(page, "page", 1),
                ApiParams.optionalInt(size, "size", 10))).build();
    }

    /**
     * Catálogo de tipos de evento.
     */
    @GET
    @Path("/types")
    public Response types() {
        return Response.ok(typesQuery.execute()).build();
    }

    /**
     * Detalle de un evento publicado.
     */
    @GET
    @Path("/{id}")
    public Response detail(@PathParam("id") String id) {
        return Response.ok(detailQuery.execute(id)).build();
    }

    /**
     * Datos para compartir un evento publicado.
     */
    @GET
    @Path("/{id}/share")
    public Response share(@PathParam("id") String id) {
        return Response.ok(shareQuery.execute(id)).build();
    }

    /**
     * Descarga del archivo iCalendar de un evento publicado.
     */
    @GET
    @Path("/{id}/calendar.ics")
    @Produces("text/calendar")
    public Response calendar(@PathParam("id") String id) {
        CalendarFileDto file = calendarQuery.execute(id);
        return Response.ok(file.content())
                .type("text/calendar; charset=utf-8")
                .header("Content-Disposition", "attachment; filename=\"" + file.fileName() + "\"")
                .build();
    }
}
