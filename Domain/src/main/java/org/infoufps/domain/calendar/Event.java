package org.infoufps.domain.calendar;

import org.infoufps.domain.calendar.EventPhase;
import org.infoufps.domain.calendar.EventType;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.EventId;
import org.infoufps.domain.calendar.EventSchedule;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Entidad de Dominio que representa un evento del calendario institucional.
 * <p>
 * El evento puede ser de un solo día o abarcar un periodo, y puede tener horas
 * (evento puntual) o carecer de ellas (evento de todo el día). El estado sigue
 * la máquina de estados compartida {@link Status}. La fase y los días restantes
 * se calculan al consultar y no se persisten.
 */
public class Event {

    /** Longitud máxima del título. */
    public static final int MAX_TITLE_LENGTH = 255;

    /** Longitud máxima del lugar. */
    public static final int MAX_LOCATION_LENGTH = 255;

    /** Longitud máxima de la descripción. */
    public static final int MAX_DESCRIPTION_LENGTH = 2000;

    /** Longitud máxima de la URL de información. */
    public static final int MAX_INFO_URL_LENGTH = 512;

    private EventId id;
    private String title;
    private EventType type;
    private String location;
    private EventSchedule schedule;
    private String description;
    private String infoUrl;
    private Status status;
    private AccountId proposedBy;
    private boolean createdByAdmin;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int revision;

    private Event(EventId id,
                  String title,
                  EventType type,
                  String location,
                  EventSchedule schedule,
                  String description,
                  String infoUrl,
                  Status status,
                  AccountId proposedBy,
                  boolean createdByAdmin,
                  LocalDateTime publishedAt,
                  LocalDateTime createdAt,
                  LocalDateTime updatedAt,
                  int revision) {
        this.id = Objects.requireNonNull(id, "El id del evento es obligatorio");
        this.title = Objects.requireNonNull(title, "El título es obligatorio");
        this.type = Objects.requireNonNull(type, "El tipo de evento es obligatorio");
        this.location = Objects.requireNonNull(location, "El lugar es obligatorio");
        this.schedule = Objects.requireNonNull(schedule, "La programación es obligatoria");
        this.description = description;
        this.infoUrl = infoUrl;
        this.status = Objects.requireNonNull(status, "El estado es obligatorio");
        this.proposedBy = Objects.requireNonNull(proposedBy, "La cuenta proponente es obligatoria");
        this.createdByAdmin = createdByAdmin;
        this.publishedAt = publishedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        this.updatedAt = Objects.requireNonNull(updatedAt, "La fecha de actualización es obligatoria");
        this.revision = Math.max(0, revision);
    }

    /**
     * Crea un nuevo evento en estado UNDER_REVIEW.
     * <p>
     * Aplica las reglas de tipo restringido y de fechas pasadas para usuarios no
     * administradores. El administrador crea el evento y luego lo publica mediante
     * {@link #publish()}.
     *
     * @param title          título obligatorio.
     * @param type           tipo de evento; si es nulo se usa GENERAL.
     * @param location       lugar obligatorio.
     * @param schedule       programación validada.
     * @param description    descripción opcional.
     * @param infoUrl        enlace opcional http/https.
     * @param proposedBy     cuenta que crea el evento.
     * @param createdByAdmin true si lo crea un administrador.
     * @return nuevo evento en revisión.
     */
    public static Event create(String title,
                               EventType type,
                               String location,
                               EventSchedule schedule,
                               String description,
                               String infoUrl,
                               AccountId proposedBy,
                               boolean createdByAdmin) {
        EventType normalizedType = type != null ? type : EventType.GENERAL;
        Objects.requireNonNull(schedule, "La programación es obligatoria");
        Objects.requireNonNull(proposedBy, "La cuenta proponente es obligatoria");

        validateTypePermission(normalizedType, createdByAdmin);
        validatePastDate(schedule, createdByAdmin);

        LocalDateTime now = BogotaTime.now();
        return new Event(
                EventId.generate(),
                normalizeRequiredText(title, "El título", MAX_TITLE_LENGTH),
                normalizedType,
                normalizeRequiredText(location, "El lugar", MAX_LOCATION_LENGTH),
                schedule,
                normalizeOptionalText(description, "La descripción", MAX_DESCRIPTION_LENGTH),
                normalizeInfoUrl(infoUrl),
                Status.UNDER_REVIEW,
                proposedBy,
                createdByAdmin,
                null,
                now,
                now,
                0
        );
    }

    /**
     * Reconstruye un evento desde persistencia.
     *
     * @return evento reconstruido.
     */
    public static Event of(EventId id,
                           String title,
                           EventType type,
                           String location,
                           EventSchedule schedule,
                           String description,
                           String infoUrl,
                           Status status,
                           AccountId proposedBy,
                           boolean createdByAdmin,
                           LocalDateTime publishedAt,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt,
                           int revision) {
        return new Event(id, title, type, location, schedule, description, infoUrl,
                status, proposedBy, createdByAdmin, publishedAt, createdAt, updatedAt, revision);
    }

    /**
     * Actualiza los datos editables del evento e incrementa la revisión.
     * <p>
     * Un usuario solo puede editar su propia propuesta mientras está en revisión.
     * Un administrador puede corregir cualquier evento no archivado. La fecha de
     * publicación no cambia al editar.
     *
     * @param title       nuevo título.
     * @param type        nuevo tipo.
     * @param location    nuevo lugar.
     * @param schedule    nueva programación.
     * @param description nueva descripción.
     * @param infoUrl     nuevo enlace.
     * @param actor       cuenta que realiza la edición.
     * @param admin       true si el actor es administrador.
     */
    public void update(String title,
                       EventType type,
                       String location,
                       EventSchedule schedule,
                       String description,
                       String infoUrl,
                       AccountId actor,
                       boolean admin) {
        if (status == Status.ARCHIVED) {
            throw new DomainException("Un evento archivado no puede editarse ni reenviarse");
        }
        Objects.requireNonNull(actor, "La cuenta que edita es obligatoria");
        Objects.requireNonNull(schedule, "La programación es obligatoria");

        if (!admin) {
            if (!proposedBy.equals(actor)) {
                throw new ForbiddenException("Solo el autor puede editar su propia propuesta");
            }
            if (status != Status.UNDER_REVIEW) {
                throw new DomainException("Solo puedes editar tu propuesta mientras está en revisión");
            }
        }

        EventType normalizedType = type != null ? type : EventType.GENERAL;
        validateTypePermission(normalizedType, admin);
        validatePastDate(schedule, admin);

        this.title = normalizeRequiredText(title, "El título", MAX_TITLE_LENGTH);
        this.type = normalizedType;
        this.location = normalizeRequiredText(location, "El lugar", MAX_LOCATION_LENGTH);
        this.schedule = schedule;
        this.description = normalizeOptionalText(description, "La descripción", MAX_DESCRIPTION_LENGTH);
        this.infoUrl = normalizeInfoUrl(infoUrl);
        this.revision++;
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Publica el evento si el estado actual lo permite. Fija {@code publishedAt}
     * únicamente la primera vez; las ediciones posteriores no lo modifican.
     */
    public void publish() {
        if (!status.canTransitionTo(Status.PUBLISHED)) {
            throw new DomainException("No se puede publicar un evento desde el estado actual: " + status);
        }
        this.status = Status.PUBLISHED;
        if (this.publishedAt == null) {
            this.publishedAt = BogotaTime.now();
        }
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Archiva el evento si el estado actual lo permite. El evento deja de ser
     * público, pero los favoritos existentes se conservan en persistencia.
     */
    public void archive() {
        if (!status.canTransitionTo(Status.ARCHIVED)) {
            throw new DomainException("No se puede archivar un evento desde el estado actual: " + status);
        }
        this.status = Status.ARCHIVED;
        this.updatedAt = BogotaTime.now();
    }

    /**
     * Calcula la fase del evento respecto de una fecha de referencia.
     *
     * @param referenceDate fecha de referencia; si es nula se usa hoy en Bogotá.
     * @return UPCOMING, ONGOING o FINISHED.
     */
    public EventPhase phase(LocalDate referenceDate) {
        LocalDate reference = referenceDate != null ? referenceDate : BogotaTime.today();
        if (reference.isBefore(schedule.startDate())) {
            return EventPhase.UPCOMING;
        }
        if (reference.isAfter(schedule.endDate())) {
            return EventPhase.FINISHED;
        }
        return EventPhase.ONGOING;
    }

    /**
     * @return fase calculada respecto de hoy en America/Bogota.
     */
    public EventPhase getPhase() {
        return phase(BogotaTime.today());
    }

    /**
     * Calcula los días restantes. Para eventos próximos cuenta hasta la fecha de
     * inicio; para eventos en curso cuenta hasta la fecha de fin (último día
     * incluido). Un evento finalizado devuelve 0.
     *
     * @param referenceDate fecha de referencia; si es nula se usa hoy en Bogotá.
     * @return días restantes.
     */
    public long daysRemaining(LocalDate referenceDate) {
        LocalDate reference = referenceDate != null ? referenceDate : BogotaTime.today();
        if (reference.isBefore(schedule.startDate())) {
            return ChronoUnit.DAYS.between(reference, schedule.startDate());
        }
        if (!reference.isAfter(schedule.endDate())) {
            return ChronoUnit.DAYS.between(reference, schedule.endDate());
        }
        return 0;
    }

    /**
     * @return días restantes calculados respecto de hoy en America/Bogota.
     */
    public long getDaysRemaining() {
        return daysRemaining(BogotaTime.today());
    }

    /**
     * @return true si el evento no tiene horas y es de todo el día.
     */
    public boolean isAllDay() {
        return schedule.isAllDay();
    }

    /**
     * @return true si el evento abarca más de un día.
     */
    public boolean isMultiDay() {
        return schedule.isMultiDay();
    }

    private static void validateTypePermission(EventType type, boolean admin) {
        if (type.isAdminOnly() && !admin) {
            throw new DomainException("Solo un administrador puede crear eventos de tipo " + type.getLabel());
        }
    }

    private static void validatePastDate(EventSchedule schedule, boolean admin) {
        if (!admin && schedule.startDate().isBefore(BogotaTime.today())) {
            throw new DomainException("No puedes proponer eventos con fecha pasada");
        }
    }

    private static String normalizeRequiredText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new DomainException(field + " es obligatorio");
        }
        String normalized = value.strip();
        if (normalized.length() > maxLength) {
            throw new DomainException(field + " no puede exceder los " + maxLength + " caracteres");
        }
        return normalized;
    }

    private static String normalizeOptionalText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.strip();
        if (normalized.length() > maxLength) {
            throw new DomainException(field + " no puede exceder los " + maxLength + " caracteres");
        }
        return normalized;
    }

    private static String normalizeInfoUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.strip();
        if (normalized.length() > MAX_INFO_URL_LENGTH) {
            throw new DomainException("La URL de información no puede exceder los " + MAX_INFO_URL_LENGTH + " caracteres");
        }
        try {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();
            boolean validScheme = scheme != null
                    && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
            if (!validScheme || uri.getHost() == null || uri.getHost().isBlank()) {
                throw new DomainException("La URL de información solo acepta http o https");
            }
        } catch (URISyntaxException ex) {
            throw new DomainException("La URL de información no es válida");
        }
        return normalized;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public EventId getId() { return id; }
    public String getTitle() { return title; }
    public EventType getType() { return type; }
    public String getLocation() { return location; }
    public EventSchedule getSchedule() { return schedule; }
    public String getDescription() { return description; }
    public String getInfoUrl() { return infoUrl; }
    public Status getStatus() { return status; }
    public AccountId getProposedBy() { return proposedBy; }
    public boolean isCreatedByAdmin() { return createdByAdmin; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public int getRevision() { return revision; }

    public LocalDate getStartDate() { return schedule.startDate(); }
    public LocalDate getEndDate() { return schedule.endDate(); }
    public java.time.LocalTime getStartTime() { return schedule.startTime(); }
    public java.time.LocalTime getEndTime() { return schedule.endTime(); }
}
