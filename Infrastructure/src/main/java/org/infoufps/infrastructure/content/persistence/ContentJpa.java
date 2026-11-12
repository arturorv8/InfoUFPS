package org.infoufps.infrastructure.content.persistence;

import jakarta.persistence.*;
import org.infoufps.domain.content.ModuleType;
import org.infoufps.domain.calendar.Status;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entidad de Persistencia (JPA/Hibernate) que mapea el modelo relacional para la tabla 'contents'.
 * <p>
 * Representa el esquema de base de datos para las tarjetas de contenido y sus metadatos.
 * A diferencia del modelo de Dominio (Content), esta clase maneja constructores públicos por defecto
 * y setters mutables para cumplir con la especificación de Jakarta Persistence.
 */
@Entity
@Table(name = "contents", indexes = {
        @Index(name = "idx_contents_status", columnList = "status")
})
public class ContentJpa {

    /** Identificador primario de la tabla (UUID de 36 caracteres en formato String). */
    @Id
    @Column(name = "content_id", length = 36, nullable = false)
    private String contentId;

    /** Título principal de la publicación. */
    @Column(name = "title", length = 255, nullable = false)
    private String title;

    /** Subtítulo o resumen corto de la tarjeta. */
    @Column(name = "subtitle", length = 255)
    private String subtitle;

    /** Descripción extensa o cuerpo descriptivo almacenado como texto de longitud variable (TEXT). */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** URL de la imagen de portada o banner de la tarjeta. */
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    /** Etiqueta de categoría principal para la pill o badge superior (ej. "ACADÉMICO"). */
    @Column(name = "category_tag", length = 50)
    private String categoryTag;

    /**
     * Mapeo de la colección de etiquetas secundarias almacenadas en la tabla secundaria 'content_tags'.
     * Utiliza un LinkedHashSet para garantizar la unicidad de las etiquetas manteniendo el orden de inserción.
     */
    @ElementCollection
    @CollectionTable(
            name = "content_tags",
            joinColumns = @JoinColumn(name = "content_id")
    )
    @Column(name = "tag", length = 50, nullable = false)
    private Set<String> tags = new LinkedHashSet<>();

    /** Enlace de destino al que redirige la tarjeta cuando el usuario hace clic. */
    @Column(name = "target_url", length = 512, nullable = false)
    private String targetUrl;

    /**
     * Objeto embebido que proyecta los contadores de interacción (likes y comentarios)
     * como columnas directamente dentro de la tabla 'contents'.
     */
    @Embedded
    private InteractionStatsJpa stats;

    /**
     * Estado del ciclo de vida del contenido almacenado como cadena de texto (STRING)
     * para facilitar la legibilidad en consultas SQL directas.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "module_type", length = 50, nullable = false)
    private ModuleType moduleType;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Constructor sin argumentos requerido obligatoriamente por la especificación de JPA/Hibernate.
     */
    public ContentJpa() {

    }

    // ── Getters y Setters ────────────────────────────────────────────────

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategoryTag() { return categoryTag; }
    public void setCategoryTag(String categoryTag) { this.categoryTag = categoryTag; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public InteractionStatsJpa getStats() { return stats; }
    public void setStats(InteractionStatsJpa stats) { this.stats = stats; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public ModuleType getModuleType() {return moduleType;}
    public void setModuleType(ModuleType moduleType) {this.moduleType = moduleType;}

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
