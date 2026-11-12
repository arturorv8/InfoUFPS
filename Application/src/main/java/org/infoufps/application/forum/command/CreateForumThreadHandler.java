package org.infoufps.application.forum.command;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infoufps.application.forum.dto.AuthorSummaryDto;
import org.infoufps.application.forum.dto.ForumThreadDetailDto;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.forum.ForumThread;
import org.infoufps.domain.forum.IForumThreadRepository;
import org.infoufps.domain.shared.exceptions.DailyLimitExceededException;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.time.BogotaTime;

import java.time.LocalDateTime;

/**
 * Caso de uso para crear un nuevo hilo de discusión en el foro comunitario.
 * <p>
 * Reglas de negocio:
 * - Publicación inmediata (nace en estado PUBLISHED).
 * - Exige cuenta autenticada y activa (una cuenta suspendida no publica).
 * - Límite diario de hilos por usuario (por defecto 5, configurable).
 */
@ApplicationScoped
public class CreateForumThreadHandler {

    public static final int DEFAULT_MAX_THREADS_PER_DAY = 5;

    private final IForumThreadRepository threadRepository;
    private final IAccountRepository accountRepository;
    private final int maxThreadsPerDay;

    @Inject
    public CreateForumThreadHandler(IForumThreadRepository threadRepository,
                                    IAccountRepository accountRepository,
                                    @ConfigProperty(name = "infoufps.forum.max-threads-per-day", defaultValue = "5")
                                    int maxThreadsPerDay) {
        this.threadRepository = threadRepository;
        this.accountRepository = accountRepository;
        this.maxThreadsPerDay = maxThreadsPerDay > 0 ? maxThreadsPerDay : DEFAULT_MAX_THREADS_PER_DAY;
    }

    public CreateForumThreadHandler(IForumThreadRepository threadRepository,
                                    IAccountRepository accountRepository) {
        this(threadRepository, accountRepository, DEFAULT_MAX_THREADS_PER_DAY);
    }

    @Transactional
    public ForumThreadDetailDto execute(CreateForumThreadCommand command) {
        if (command == null) {
            throw new DomainException("El comando de creación no puede ser nulo");
        }
        if (command.authorId() == null || command.authorId().isBlank()) {
            throw new DomainException("El identificador del autor es obligatorio");
        }

        AccountId authorId = AccountId.of(command.authorId());
        Account author = accountRepository.findById(authorId)
                .orElseThrow(() -> new DomainException("El autor especificado no existe: " + command.authorId()));

        if (!author.isActive()) {
            throw new ForbiddenException("La cuenta está suspendida");
        }

        // Límite de hilos por día
        LocalDateTime startOfDay = BogotaTime.now().toLocalDate().atStartOfDay();
        long createdToday = threadRepository.countCreatedByAuthorSince(authorId, startOfDay);
        if (createdToday >= maxThreadsPerDay) {
            throw new DailyLimitExceededException("Has superado el límite diario de creación de hilos (" + maxThreadsPerDay + ")");
        }

        ForumThread thread = ForumThread.create(command.title(), command.body(), authorId);
        threadRepository.save(thread);

        AuthorSummaryDto authorDto = AuthorSummaryDto.from(authorId, author);
        return new ForumThreadDetailDto(
                thread.getId().value(),
                thread.getTitle(),
                thread.getBody(),
                authorDto,
                thread.getStatus().name(),
                thread.getCreatedAt(),
                thread.getUpdatedAt(),
                thread.isEdited(),
                0L,
                0L
        );
    }
}

