package org.infoufps.domain.content;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.infoufps.domain.content.ModuleType;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.content.ContentId;
import org.infoufps.domain.content.InteractionStats;

/**
 * Entidad de Dominio que representa la vista previa (tarjeta / card) de un contenido en el feed.
 * Encapsula la información visual ligera que se muestra al usuario en el catálogo o dashboard,
 * gestionando sus badges, enlace de destino, contadores de interacción y estado de publicación.
 */
public class Content {

    /** Identificador único e inmutable de la tarjeta de contenido. */
    private ContentId id;

    /** Título principal visible en la tarjeta. */
    private String title;

    /** Subtítulo o resumen corto mostrado debajo del título. */
    private String subtitle;

    /** Descripción breve o extracto que se muestra en la tarjeta. */
    private String description;

    /** URL de la imagen de portada, banner o miniatura representativa. */
    private String imageUrl;

    /** Categoría principal para filtrado y pill visual superior (ej. "ACADÉMICO", "ARRIENDOS", "PROYECTO"). */
    private String categoryTag;

    /** Conjunto de etiquetas/badges secundarios visibles en la tarjeta, ordenados y sin duplicados. */
    private Set<String> tags;

    /** Enlace de destino (URL externa o ruta interna) al que redirige la tarjeta al hacer clic en ella. */
    private String targetUrl;

    /** Contadores descriptivos de interacción (likes y comentarios) para mostrar en la vista previa. */
    private InteractionStats stats;

    /** Estado del ciclo de vida interno que controla si la tarjeta es visible públicamente o está en moderación. */
    private Status status;

    private ModuleType moduleType;

    /**
     * Constructor privado accesible solo a través de los métodos de fábrica estáticos (of/create).
     * Garantiza la creación de una vista previa de contenido consistente y válida.
     *
     * @param id Identificador único de la tarjeta.
     * @param title Título principal (no nulo).
     * @param subtitle Subtítulo o resumen opcional.
     * @param description Descripción opcional.
     * @param imageUrl Ruta de la miniatura o banner.
     * @param categoryTag Categoría principal para la pill superior.
     * @param tags Colección de badges informativos ordenados.
     * @param targetUrl Ruta de navegación al presionar la tarjeta (no nula).
     * @param stats Contadores numéricos de interacción; si es nulo se inicializa en 0.
     * @param status Estado de moderación; si es nulo se asigna UNDER_REVIEW por defecto.
     */
    private Content(ContentId id,
                    String title,
                    String subtitle,
                    String description,
                    String imageUrl,
                    String categoryTag,
                    Set<String> tags,
                    String targetUrl,
                    InteractionStats stats,
                    Status status,
                    ModuleType moduleType) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.title = Objects.requireNonNull(title, "Titulo es obligatorio");
        this.subtitle = subtitle;
        this.description = description;
        this.imageUrl = imageUrl;
        this.categoryTag = categoryTag;
        this.moduleType = Objects.requireNonNull(moduleType, "Modulo es obligatorio");
        this.tags = tags != null ? new LinkedHashSet<>(tags) : new LinkedHashSet<>();
        this.targetUrl = Objects.requireNonNull(targetUrl, "La ruta de destino (targetUrl) es obligatoria");
        this.stats = stats != null ? stats : InteractionStats.empty();
        this.status = status != null ? status : Status.UNDER_REVIEW;
    }

    /**
     * Reconstruye la vista previa de un contenido desde la base de datos a través de los repositorios.
     *
     * @return Instancia de Content con todos los datos cargados para pintar la tarjeta.
     */
    public static Content of(ContentId id, String title, String subtitle, String description,
                             String imageUrl, String categoryTag, Set<String> tags,
                             String targetUrl, InteractionStats stats, Status status, ModuleType moduleType) {
        return new Content(
                id, title, subtitle, description, imageUrl,
                categoryTag, tags, targetUrl, stats, status, moduleType
        );
    }

    /**
     * Crea una nueva tarjeta de contenido desde el formulario de publicación/moderación.
     * Genera un nuevo ContentId automáticamente y la deja lista para ingresar al flujo de revisión.
     *
     * @return Nueva tarjeta de contenido inicializada.
     */
    public static Content create(String title, String subtitle, String description,
                                 String imageUrl, String categoryTag, Set<String> tags,
                                 String targetUrl, InteractionStats stats, Status status, ModuleType moduleType) {
        return new Content(
                ContentId.generate(),
                title, subtitle, description, imageUrl,
                categoryTag, tags, targetUrl, stats, status, moduleType
        );
    }

    /**
     * Regla de negocio: Aprueba la tarjeta de contenido para que aparezca en el feed público de los usuarios.
     * Requiere que el contenido esté actualmente en revisión (UNDER_REVIEW).
     *
     * @throws DomainException Si la tarjeta no está en estado UNDER_REVIEW.
     */
    public void publish() {
        if (!this.status.canTransitionTo(Status.PUBLISHED)) {
            throw new DomainException("No se puede publicar un contenido desde el estado actual: " + this.status);
        }
        this.status = Status.PUBLISHED;
    }

    /**
     * Regla de negocio: Oculta la tarjeta del feed público pasando su estado a ARCHIVED.
     * Mantiene los datos en la base de datos sin mostrarlos a los usuarios finales.
     */
    public void archive() {
        if (!this.status.canTransitionTo(Status.ARCHIVED)) {
            throw new DomainException("No se puede archivar un contenido desde el estado actual: " + this.status);
        }
        this.status = Status.ARCHIVED;
    }

    /**
     * Incrementa en 1 el contador de comentarios asociados a este contenido.
     */
    public void incrementComments() {
        this.stats = this.stats.incrementarComentarios();
    }

    /**
     * Decrementa en 1 el contador de comentarios asociados a este contenido.
     */
    public void decrementComments() {
        this.stats = this.stats.decrementarComentarios();
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public ContentId getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getCategoryTag() { return categoryTag; }

    /**
     * Expone el conjunto de badges informativos de la tarjeta.
     * Devuelve un conjunto inmodificable para proteger el estado de la entidad.
     */
    public Set<String> getTags() { return Collections.unmodifiableSet(tags); }

    public String getTargetUrl() { return targetUrl; }
    public InteractionStats getStats() { return stats; }
    public Status getStatus() { return status; }
    public ModuleType getModuleType() {return moduleType;}
}
