package org.infoufps.application.calendar;

import org.infoufps.domain.calendar.Event;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.application.calendar.PublishedEventLoader;
import org.infoufps.application.calendar.FixedCalendarSettings;
import org.infoufps.application.calendar.InMemoryEventRepository;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GetSharePageQuery: página HTML de vista previa")
class GetSharePageQueryTest {

    private InMemoryEventRepository repository;
    private GetSharePageQuery query;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEventRepository();
        query = new GetSharePageQuery(new PublishedEventLoader(repository), FixedCalendarSettings.defaults());
    }

    private Event save(boolean publish, String title) {
        LocalDate day = BogotaTime.today().plusDays(5);
        Event event = Event.create(title, EventType.ACADEMICO, "Bloque A",
                EventSchedule.of(day, day, null, null), null, null,
                AccountId.of("account-1"), true);
        if (publish) {
            event.publish();
        }
        return repository.save(event);
    }

    @Test
    @DisplayName("Escapa el título y la descripción del evento")
    void escapesUserContent() {
        Event event = save(true, "<script>alert(\"x\")</script>");

        String html = query.execute(event.getId().value()).html();

        assertFalse(html.contains("<script>alert"));
        assertTrue(html.contains("&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;"));
    }

    @Test
    @DisplayName("Incluye los metadatos Open Graph y la URL del evento")
    void includesOpenGraphMetadata() {
        Event event = save(true, "Feria de ciencia");

        String html = query.execute(event.getId().value()).html();

        assertTrue(html.contains("<meta property=\"og:title\" content=\"Feria de ciencia\">"));
        assertTrue(html.contains("https://info.ufps.edu.co/eventos/" + event.getId().value()));
        assertTrue(html.contains("https://info.ufps.edu.co/logo.png"));
    }

    @Test
    @DisplayName("Un evento no publicado no genera página")
    void unpublishedEventIsNotFound() {
        Event event = save(false, "En revisión");

        assertThrows(NotFoundException.class, () -> query.execute(event.getId().value()));
    }

    @Test
    @DisplayName("Un identificador inexistente produce 404")
    void unknownEventIsNotFound() {
        assertThrows(NotFoundException.class, () -> query.execute("no-existe"));
    }
}
