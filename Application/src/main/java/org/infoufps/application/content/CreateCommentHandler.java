package org.infoufps.application.content;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.content.Comment;
import org.infoufps.domain.content.CommentTargetType;
import org.infoufps.domain.content.ICommentRepository;
import org.infoufps.domain.content.IContentRepository;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caso de uso para registrar comentarios en destinos genéricos (polimórficos).
 * <p>
 * Reglas de negocio aplicadas:
 * 1. Desacoplado de contenidos: delega la validación de existencia y estado a {@link ICommentTargetValidator}.
 * 2. Verifica que el autor exista y no se encuentre con cuenta suspendida.
 * 3. Aplica control antispam limitando la cantidad máxima de comentarios por minuto por usuario (consultado en BD).
 * 4. Crea y persiste el comentario con estado {@code PUBLISHED}.
 * 5. No realiza incrementos atómicos directos en el destino; el conteo es calculado bajo demanda.
 */
@ApplicationScoped
public class CreateCommentHandler {

    public static final int DEFAULT_MAX_COMMENTS_PER_MINUTE = 5;

    private final ICommentRepository commentRepository;
    private final IAccountRepository accountRepository;
    private final Map<CommentTargetType, ICommentTargetValidator> validators;
    private final int maxCommentsPerMinute;

    @Inject
    public CreateCommentHandler(ICommentRepository commentRepository,
                                IAccountRepository accountRepository,
                                Instance<ICommentTargetValidator> targetValidators,
                                @ConfigProperty(name = "infoufps.comments.max-per-minute", defaultValue = "5")
                                int maxCommentsPerMinute) {
        this.commentRepository = commentRepository;
        this.accountRepository = accountRepository;
        this.maxCommentsPerMinute = maxCommentsPerMinute > 0 ? maxCommentsPerMinute : DEFAULT_MAX_COMMENTS_PER_MINUTE;
        this.validators = new HashMap<>();
        for (ICommentTargetValidator validator : targetValidators) {
            this.validators.put(validator.targetType(), validator);
        }
    }

    /**
     * Constructor para pruebas unitarias con lista explícita de validadores y límite antispam.
     */
    public CreateCommentHandler(ICommentRepository commentRepository,
                                IAccountRepository accountRepository,
                                List<ICommentTargetValidator> targetValidators,
                                int maxCommentsPerMinute) {
        this.commentRepository = commentRepository;
        this.accountRepository = accountRepository;
        this.maxCommentsPerMinute = maxCommentsPerMinute > 0 ? maxCommentsPerMinute : DEFAULT_MAX_COMMENTS_PER_MINUTE;
        this.validators = new HashMap<>();
        if (targetValidators != null) {
            for (ICommentTargetValidator validator : targetValidators) {
                this.validators.put(validator.targetType(), validator);
            }
        }
    }

    /**
     * Constructor de retrocompatibilidad para pruebas unitarias previas basadas en IContentRepository.
     */
    public CreateCommentHandler(ICommentRepository commentRepository,
                                IContentRepository contentRepository,
                                IAccountRepository accountRepository) {
        this(
                commentRepository,
                accountRepository,
                List.of(new ContentCommentTargetValidator(contentRepository)),
                DEFAULT_MAX_COMMENTS_PER_MINUTE
        );
    }

    /**
     * Ejecuta el registro del comentario.
     *
     * @param command Datos de creación.
     * @return DTO del comentario creado.
     */
    @Transactional
    public CommentDto execute(CreateCommentCommand command) {
        if (command == null) {
            throw new DomainException("El comando de creación no puede ser nulo");
        }
        if (command.targetType() == null) {
            throw new DomainException("El tipo de destino es obligatorio");
        }
        if (command.targetId() == null || command.targetId().isBlank()) {
            throw new DomainException("El contentId es obligatorio");
        }
        if (command.authorId() == null || command.authorId().isBlank()) {
            throw new DomainException("El authorId es obligatorio");
        }

        AccountId authorId = AccountId.of(command.authorId());
        Account author = accountRepository.findById(authorId)
                .orElseThrow(() -> new DomainException("El autor especificado no existe: " + command.authorId()));

        if (!author.isActive()) {
            throw new ForbiddenException("La cuenta está suspendida");
        }

        // Control antispam: límite de comentarios por minuto en BD
        LocalDateTime oneMinuteAgo = BogotaTime.now().minusMinutes(1);
        long recentComments = commentRepository.countByAuthorIdSince(authorId, oneMinuteAgo);
        if (recentComments >= maxCommentsPerMinute) {
            throw new DomainException("Has superado el límite de comentarios por minuto (" + maxCommentsPerMinute + ")");
        }

        // Validación del destino mediante componente desacoplado
        ICommentTargetValidator validator = validators.get(command.targetType());
        if (validator == null) {
            throw new DomainException("Tipo de destino no soportado: " + command.targetType());
        }
        validator.validateTargetExistsAndPublished(command.targetId());

        Comment comment = Comment.create(command.targetType(), command.targetId(), authorId, command.text());
        commentRepository.save(comment);

        return CommentDto.from(comment);
    }
}
