package org.infoufps.domain.academic;

import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.TipoMateria;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.MateriaId;

import java.time.LocalDateTime;
import java.util.Objects;

public class Materia {

    private final MateriaId id;
    private String nombre;
    private String nombreNormalizado;
    private String codigo;
    private Integer creditos;
    private TipoMateria tipo;
    private EstadoModeracion estado;
    private final AccountId proponenteId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Materia(MateriaId id,
                    String nombre,
                    String nombreNormalizado,
                    String codigo,
                    Integer creditos,
                    TipoMateria tipo,
                    EstadoModeracion estado,
                    AccountId proponenteId,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "El ID de la materia es obligatorio");
        this.nombre = validateNombre(nombre);
        this.nombreNormalizado = (nombreNormalizado != null && !nombreNormalizado.isBlank())
                ? nombreNormalizado
                : TextNormalizer.normalizeAndSortWords(nombre);
        this.codigo = validateAndNormalizeCodigo(codigo);
        this.creditos = validateCreditos(creditos);
        this.tipo = tipo;
        this.estado = estado != null ? estado : EstadoModeracion.EN_REVISION;
        this.proponenteId = proponenteId;
        this.createdAt = createdAt != null ? createdAt : BogotaTime.now();
        this.updatedAt = updatedAt;
    }

    /**
     * Propuesta de materia creada por un usuario.
     * Solo el nombre es estrictamente obligatorio (reglas 3 y 9).
     */
    public static Materia propose(String nombre, String codigo, Integer creditos, TipoMateria tipo, AccountId proponenteId) {
        if (proponenteId == null) {
            throw new DomainException("El proponente es obligatorio al proponer una materia");
        }
        return new Materia(
                MateriaId.generate(),
                nombre,
                null,
                codigo,
                creditos,
                tipo,
                EstadoModeracion.EN_REVISION,
                proponenteId,
                BogotaTime.now(),
                null
        );
    }

    /**
     * Creación directa por un administrador. Inicia en PUBLICADO.
     */
    public static Materia createByAdmin(String nombre, String codigo, Integer creditos, TipoMateria tipo) {
        return new Materia(
                MateriaId.generate(),
                nombre,
                null,
                codigo,
                creditos,
                tipo,
                EstadoModeracion.PUBLICADO,
                null,
                BogotaTime.now(),
                null
        );
    }

    /**
     * Reconstrucción desde persistencia.
     */
    public static Materia of(MateriaId id,
                             String nombre,
                             String nombreNormalizado,
                             String codigo,
                             Integer creditos,
                             TipoMateria tipo,
                             EstadoModeracion estado,
                             AccountId proponenteId,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        return new Materia(
                id,
                nombre,
                nombreNormalizado,
                codigo,
                creditos,
                tipo,
                estado,
                proponenteId,
                createdAt,
                updatedAt
        );
    }

    public void updateProposal(String nuevoNombre, String nuevoCodigo, Integer nuevosCreditos, TipoMateria nuevoTipo, AccountId editorId) {
        if (this.estado != EstadoModeracion.EN_REVISION) {
            throw new DomainException("Solo se pueden editar propuestas en revisión");
        }
        if (this.proponenteId == null || !this.proponenteId.equals(editorId)) {
            throw new ForbiddenException("No tienes permiso para editar esta propuesta");
        }
        this.nombre = validateNombre(nuevoNombre);
        this.nombreNormalizado = TextNormalizer.normalizeAndSortWords(nuevoNombre);
        this.codigo = validateAndNormalizeCodigo(nuevoCodigo);
        this.creditos = validateCreditos(nuevosCreditos);
        this.tipo = nuevoTipo;
        this.updatedAt = BogotaTime.now();
    }

    public void adminUpdate(String nuevoNombre, String nuevoCodigo, Integer nuevosCreditos, TipoMateria nuevoTipo) {
        if (this.estado == EstadoModeracion.ARCHIVADO) {
            throw new DomainException("No se puede editar una materia archivada");
        }
        this.nombre = validateNombre(nuevoNombre);
        this.nombreNormalizado = TextNormalizer.normalizeAndSortWords(nuevoNombre);
        this.codigo = validateAndNormalizeCodigo(nuevoCodigo);
        this.creditos = validateCreditos(nuevosCreditos);
        this.tipo = nuevoTipo;
        this.updatedAt = BogotaTime.now();
    }

    public void publish() {
        if (!this.estado.canTransitionTo(EstadoModeracion.PUBLICADO)) {
            throw new DomainException("No se puede publicar la materia desde el estado actual: " + this.estado);
        }
        this.estado = EstadoModeracion.PUBLICADO;
        this.updatedAt = BogotaTime.now();
    }

    public void archive() {
        if (!this.estado.canTransitionTo(EstadoModeracion.ARCHIVADO)) {
            throw new DomainException("No se puede archivar la materia desde el estado actual: " + this.estado);
        }
        this.estado = EstadoModeracion.ARCHIVADO;
        this.updatedAt = BogotaTime.now();
    }

    private static String validateNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("El nombre de la materia es obligatorio");
        }
        String stripped = nombre.strip();
        if (stripped.length() < 2 || stripped.length() > 150) {
            throw new DomainException("El nombre de la materia debe tener entre 2 y 150 caracteres");
        }
        return stripped;
    }

    private static String validateAndNormalizeCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        String stripped = codigo.strip().toUpperCase();
        if (stripped.length() > 30) {
            throw new DomainException("El código de la materia no puede exceder 30 caracteres");
        }
        return stripped;
    }

    private static Integer validateCreditos(Integer creditos) {
        if (creditos == null) {
            return null;
        }
        if (creditos <= 0) {
            throw new DomainException("Los créditos de la materia deben ser un entero positivo");
        }
        return creditos;
    }

    public MateriaId getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNombreNormalizado() { return nombreNormalizado; }
    public String getCodigo() { return codigo; }
    public Integer getCreditos() { return creditos; }
    public TipoMateria getTipo() { return tipo; }
    public EstadoModeracion getEstado() { return estado; }
    public AccountId getProponenteId() { return proponenteId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

