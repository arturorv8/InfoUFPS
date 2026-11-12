package org.infoufps.application.content;

import jakarta.transaction.Transactional;
import org.infoufps.application.ApplicationUseCasesTest;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.Role;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.content.Comment;
import org.infoufps.domain.content.CommentId;
import org.infoufps.domain.content.CommentTargetType;
import org.infoufps.domain.content.Content;
import org.infoufps.domain.content.ContentId;
import org.infoufps.domain.content.ICommentRepository;
import org.infoufps.domain.content.IContentRepository;
import org.infoufps.domain.content.InteractionStats;
import org.infoufps.domain.content.ModuleType;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PagedResult;
import org.infoufps.domain.shared.time.BogotaTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Comentarios genéricos: validación polimórfica, ciclo de vida, antispam y permisos")
class CommentHandlerTest {

    private InMemoryContentRepository contentRepository;
    private InMemoryCommentRepository commentRepository;
    private ApplicationUseCasesTest.InMemoryAccountRepository accountRepository;

    private CreateCommentHandler handler;
    private UpdateCommentHandler updateHandler;
    private DeleteCommentHandler deleteHandler;
    private GetCommentsByTargetQuery getCommentsByTargetQuery;
    private GetCommentsByContentQuery getCommentsByContentQuery;

    private AccountId author;
    private AccountId otherUser;
    private AccountId admin;
    private Content published;
    private Content underReview;

    // Destino simulado de hilo de foro para pruebas polimórficas
    private final String publishedThreadId = "thread-published-1";
    private final String unpublishedThreadId = "thread-review-2";

    @BeforeEach
    void setUp() {
        contentRepository = new InMemoryContentRepository();
        commentRepository = new InMemoryCommentRepository();
        accountRepository = new ApplicationUseCasesTest.InMemoryAccountRepository();

        Account authorAcc = accountRepository.save(Account.register(
                "comentador@ufps.edu.co", "hash", "Comentador"));
        author = authorAcc.getAccountId();

        Account otherAcc = accountRepository.save(Account.register(
                "otro@ufps.edu.co", "hash", "Otro Usuario"));
        otherUser = otherAcc.getAccountId();

        Account adminAcc = accountRepository.save(Account.register(
                "admin@ufps.edu.co", "hash", "Administrador", Role.ADMIN));
        admin = adminAcc.getAccountId();

        published = content(Status.PUBLISHED);
        underReview = content(Status.UNDER_REVIEW);
        contentRepository.save(published);
        contentRepository.save(underReview);

        // Validador de contenido
        ContentCommentTargetValidator contentValidator = new ContentCommentTargetValidator(contentRepository);

        // Validador de hilo de foro simulado para la prueba polimórfica
        ICommentTargetValidator threadValidator = new ICommentTargetValidator() {
            @Override
            public CommentTargetType targetType() {
                return CommentTargetType.FORUM_THREAD;
            }

            @Override
            public void validateTargetExistsAndPublished(String targetId) {
                if (publishedThreadId.equals(targetId)) {
                    return; // Existe y está publicado
                }
                if (unpublishedThreadId.equals(targetId)) {
                    throw new DomainException("Solo se pueden comentar hilos publicados");
                }
                throw new DomainException("El hilo especificado no existe: " + targetId);
            }
        };

        handler = new CreateCommentHandler(
                commentRepository,
                accountRepository,
                List.of(contentValidator, threadValidator),
                5
        );

        updateHandler = new UpdateCommentHandler(commentRepository);
        deleteHandler = new DeleteCommentHandler(commentRepository);
        getCommentsByTargetQuery = new GetCommentsByTargetQuery(commentRepository);
        getCommentsByContentQuery = new GetCommentsByContentQuery(commentRepository);
    }

    private Content content(Status status) {
        return Content.create("Contenido", "Subtítulo", "Descripción", null, "ACADEMICO",
                Set.of(), "https://info.ufps.edu.co", InteractionStats.empty(), status,
                ModuleType.LA_VITRINA);
    }

    // ── Pruebas existentes (retrocompatibilidad) ──────────────────────────

    @Test
    @DisplayName("Permite comentar un contenido publicado e incrementa el contador calculado")
    void createsCommentOnPublishedContent() {
        CommentDto dto = handler.execute(new CreateCommentCommand(
                published.getId().value(), author.value(), "Buen aporte"));

        assertEquals("Buen aporte", dto.text());
        assertEquals(CommentTargetType.CONTENT.name(), dto.targetType());
        assertEquals(published.getId().value(), dto.targetId());
        assertEquals(published.getId().value(), dto.contentId());
        assertEquals(Status.PUBLISHED.name(), dto.status());
        assertEquals(1, commentRepository.countByContentId(published.getId()));
        assertEquals(1, commentRepository.countPublishedByTarget(CommentTargetType.CONTENT, published.getId().value()));
    }

    @Test
    @DisplayName("Rechaza comentar un contenido no publicado sin alterar el contador")
    void rejectsCommentOnUnpublishedContent() {
        assertThrows(DomainException.class, () -> handler.execute(new CreateCommentCommand(
                underReview.getId().value(), author.value(), "Comentario")));

        assertEquals(0, commentRepository.countByContentId(underReview.getId()));
    }

    @Test
    @DisplayName("Rechaza comentar con un autor inexistente")
    void rejectsUnknownAuthor() {
        assertThrows(DomainException.class, () -> handler.execute(new CreateCommentCommand(
                published.getId().value(), AccountId.generate().value(), "Comentario")));

        assertEquals(0, commentRepository.countByContentId(published.getId()));
    }

    @Test
    @DisplayName("La creación de comentarios es transaccional")
    void executeIsTransactional() throws NoSuchMethodException {
        Method method = CreateCommentHandler.class.getMethod("execute", CreateCommentCommand.class);
        assertNotNull(method.getAnnotation(Transactional.class));
    }

    @Test
    @DisplayName("searchPublished busca por título o descripción y respeta límite")
    void testSearchPublishedContents() {
        for (int i = 1; i <= 5; i++) {
            contentRepository.save(Content.create("Noticia " + i, "Sub", "Descripción de prueba " + i, null,
                    "NOTICIAS", Set.of(), "https://ufps.edu.co/n" + i, InteractionStats.empty(),
                    Status.PUBLISHED, ModuleType.LA_VITRINA));
        }

        List<Content> results = contentRepository.searchPublished("Noticia", 3);
        assertEquals(3, results.size());

        List<Content> byDesc = contentRepository.searchPublished("prueba 2", 10);
        assertEquals(1, byDesc.size());
        assertEquals("Noticia 2", byDesc.get(0).getTitle());
    }

    // ── Nuevas pruebas Fase A ─────────────────────────────────────────────

    @Test
    @DisplayName("El comentario solo se crea si el destino existe y está publicado, para cada tipo")
    void commentOnlyCreatedIfTargetExistsAndIsPublishedForEachType() {
        // CONTENT válido
        CommentDto cDto = handler.execute(new CreateCommentCommand(
                CommentTargetType.CONTENT, published.getId().value(), author.value(), "Contenido OK"));
        assertEquals(Status.PUBLISHED.name(), cDto.status());

        // CONTENT no publicado
        assertThrows(DomainException.class, () -> handler.execute(new CreateCommentCommand(
                CommentTargetType.CONTENT, underReview.getId().value(), author.value(), "Contenido no publicado")));

        // CONTENT inexistente
        assertThrows(DomainException.class, () -> handler.execute(new CreateCommentCommand(
                CommentTargetType.CONTENT, ContentId.generate().value(), author.value(), "Contenido inexistente")));

        // FORUM_THREAD válido
        CommentDto tDto = handler.execute(new CreateCommentCommand(
                CommentTargetType.FORUM_THREAD, publishedThreadId, author.value(), "Hilo OK"));
        assertEquals(Status.PUBLISHED.name(), tDto.status());

        // FORUM_THREAD no publicado
        assertThrows(DomainException.class, () -> handler.execute(new CreateCommentCommand(
                CommentTargetType.FORUM_THREAD, unpublishedThreadId, author.value(), "Hilo no publicado")));

        // FORUM_THREAD inexistente
        assertThrows(DomainException.class, () -> handler.execute(new CreateCommentCommand(
                CommentTargetType.FORUM_THREAD, "thread-inexistente", author.value(), "Hilo inexistente")));
    }

    @Test
    @DisplayName("El contador calculado excluye comentarios archivados (borrado lógico)")
    void calculatedCounterExcludesArchivedComments() {
        CommentDto c1 = handler.execute(new CreateCommentCommand(
                published.getId().value(), author.value(), "Comentario 1"));
        CommentDto c2 = handler.execute(new CreateCommentCommand(
                published.getId().value(), author.value(), "Comentario 2"));

        assertEquals(2, commentRepository.countByContentId(published.getId()));

        // Borrado lógico del comentario 1
        deleteHandler.execute(new DeleteCommentCommand(c1.commentId(), author.value(), false));

        Comment archived = commentRepository.findById(CommentId.of(c1.commentId())).orElseThrow();
        assertEquals(Status.ARCHIVED, archived.getStatus());
        assertTrue(archived.isArchived());

        // El contador ahora devuelve 1
        assertEquals(1, commentRepository.countByContentId(published.getId()));
        assertEquals(1, commentRepository.countPublishedByTarget(CommentTargetType.CONTENT, published.getId().value()));
    }

    @Test
    @DisplayName("Orden correcto del listado: del más antiguo al más reciente (created_at ASC)")
    void listOrderingFromOldestToNewest() {
        LocalDateTime t1 = BogotaTime.now().minusHours(3);
        LocalDateTime t2 = BogotaTime.now().minusHours(2);
        LocalDateTime t3 = BogotaTime.now().minusHours(1);

        Comment comment1 = Comment.of(CommentId.generate(), CommentTargetType.FORUM_THREAD, publishedThreadId,
                author, "Primero", Status.PUBLISHED, t1, null);
        Comment comment2 = Comment.of(CommentId.generate(), CommentTargetType.FORUM_THREAD, publishedThreadId,
                author, "Segundo", Status.PUBLISHED, t2, null);
        Comment comment3 = Comment.of(CommentId.generate(), CommentTargetType.FORUM_THREAD, publishedThreadId,
                author, "Tercero", Status.PUBLISHED, t3, null);

        // Se guardan desordenados
        commentRepository.save(comment2);
        commentRepository.save(comment3);
        commentRepository.save(comment1);

        List<CommentDto> list = getCommentsByTargetQuery.execute(CommentTargetType.FORUM_THREAD, publishedThreadId);
        assertEquals(3, list.size());
        assertEquals("Primero", list.get(0).text());
        assertEquals("Segundo", list.get(1).text());
        assertEquals("Tercero", list.get(2).text());

        // Consulta paginada
        PagedResult<CommentDto> page = getCommentsByTargetQuery.execute(
                CommentTargetType.FORUM_THREAD, publishedThreadId, PageRequest.of(1, 2));
        assertEquals(3, page.total());
        assertEquals(2, page.items().size());
        assertEquals("Primero", page.items().get(0).text());
        assertEquals("Segundo", page.items().get(1).text());
    }

    @Test
    @DisplayName("Permisos de editar y borrar: solo autor o admin")
    void editAndDeletePermissions() {
        CommentDto created = handler.execute(new CreateCommentCommand(
                published.getId().value(), author.value(), "Texto original"));

        // Otro usuario no puede editar
        assertThrows(ForbiddenException.class, () -> updateHandler.execute(
                new UpdateCommentCommand(created.commentId(), otherUser.value(), false, "Texto atacante")));

        // Autor puede editar
        CommentDto updatedByAuthor = updateHandler.execute(
                new UpdateCommentCommand(created.commentId(), author.value(), false, "Texto editado por autor"));
        assertEquals("Texto editado por autor", updatedByAuthor.text());
        assertNotNull(updatedByAuthor.updatedAt());

        // Admin puede editar comentario ajeno
        CommentDto updatedByAdmin = updateHandler.execute(
                new UpdateCommentCommand(created.commentId(), admin.value(), true, "Texto moderado por admin"));
        assertEquals("Texto moderado por admin", updatedByAdmin.text());

        // Otro usuario no puede borrar
        assertThrows(ForbiddenException.class, () -> deleteHandler.execute(
                new DeleteCommentCommand(created.commentId(), otherUser.value(), false)));

        // Admin puede borrar
        deleteHandler.execute(new DeleteCommentCommand(created.commentId(), admin.value(), true));
        assertTrue(commentRepository.findById(CommentId.of(created.commentId())).orElseThrow().isArchived());

        // Editar o borrar comentario inexistente produce NotFoundException (404)
        assertThrows(NotFoundException.class, () -> updateHandler.execute(
                new UpdateCommentCommand("inexistente", author.value(), false, "Texto")));
        assertThrows(NotFoundException.class, () -> deleteHandler.execute(
                new DeleteCommentCommand("inexistente", author.value(), false)));
    }

    @Test
    @DisplayName("Validación de texto en edición: no vacío y máx. 1000 caracteres")
    void updateCommentValidatesTextLength() {
        CommentDto created = handler.execute(new CreateCommentCommand(
                published.getId().value(), author.value(), "Texto base"));

        assertThrows(DomainException.class, () -> updateHandler.execute(
                new UpdateCommentCommand(created.commentId(), author.value(), false, "   ")));

        String toolong = "a".repeat(1001);
        assertThrows(DomainException.class, () -> updateHandler.execute(
                new UpdateCommentCommand(created.commentId(), author.value(), false, toolong)));
    }

    @Test
    @DisplayName("Antispam: límite máximo de comentarios por minuto en BD")
    void rateLimitPerMinuteEnforced() {
        // Se permiten 5 comentarios por minuto
        for (int i = 1; i <= 5; i++) {
            handler.execute(new CreateCommentCommand(
                    published.getId().value(), author.value(), "Comentario antispam " + i));
        }

        // El 6to comentario en el mismo minuto es rechazado
        assertThrows(DomainException.class, () -> handler.execute(new CreateCommentCommand(
                published.getId().value(), author.value(), "Comentario spam 6")));
    }

    @Test
    @DisplayName("Cuenta suspendida es rechazada y no puede comentar")
    void suspendedAccountCannotComment() {
        Account acc = accountRepository.findById(author).orElseThrow();
        acc.suspend();
        assertFalse(acc.isActive());

        assertThrows(ForbiddenException.class, () -> handler.execute(new CreateCommentCommand(
                published.getId().value(), author.value(), "Intento desde cuenta suspendida")));
    }

    // ── Repositorios en memoria para pruebas unitarias ─────────────────────

    static class InMemoryContentRepository implements IContentRepository {
        private final Map<String, Content> db = new LinkedHashMap<>();

        @Override public Content save(Content content) { db.put(content.getId().value(), content); return content; }
        @Override public Optional<Content> findById(ContentId id) { return Optional.ofNullable(db.get(id.value())); }
        @Override public List<Content> findAllPublished() {
            return db.values().stream().filter(c -> c.getStatus() == Status.PUBLISHED).toList();
        }
        @Override public List<Content> findByStatus(Status status) {
            return db.values().stream().filter(c -> c.getStatus() == status).toList();
        }
        @Override public List<Content> findPublishedByModuleType(ModuleType moduleType) {
            return db.values().stream()
                    .filter(c -> c.getStatus() == Status.PUBLISHED && c.getModuleType() == moduleType)
                    .toList();
        }
        @Override
        public List<Content> searchPublished(String query, int limit) {
            int max = limit > 0 ? limit : 10;
            return db.values().stream()
                    .filter(c -> c.getStatus() == Status.PUBLISHED)
                    .filter(c -> query == null || query.isBlank()
                            || (c.getTitle() != null && c.getTitle().toLowerCase().contains(query.trim().toLowerCase()))
                            || (c.getDescription() != null && c.getDescription().toLowerCase().contains(query.trim().toLowerCase())))
                    .limit(max)
                    .toList();
        }
        @Override public void deleteById(ContentId id) { db.remove(id.value()); }
        @Override public boolean existsById(ContentId id) { return db.containsKey(id.value()); }
        @Override public void incrementCommentsCount(ContentId id) {
            Content content = db.get(id.value());
            if (content != null) {
                content.incrementComments();
            }
        }
        @Override public void decrementCommentsCount(ContentId id) {
            Content content = db.get(id.value());
            if (content != null) {
                content.decrementComments();
            }
        }
    }

    static class InMemoryCommentRepository implements ICommentRepository {
        private final Map<String, Comment> db = new LinkedHashMap<>();

        @Override
        public Comment save(Comment comment) {
            db.put(comment.getId().value(), comment);
            return comment;
        }

        @Override
        public Optional<Comment> findById(CommentId id) {
            return Optional.ofNullable(db.get(id.value()));
        }

        @Override
        public List<Comment> findByContentId(ContentId contentId) {
            return findPublishedByTarget(CommentTargetType.CONTENT, contentId.value());
        }

        @Override
        public List<Comment> findPublishedByTarget(CommentTargetType targetType, String targetId) {
            return db.values().stream()
                    .filter(c -> c.getTargetType() == targetType
                            && targetId.equals(c.getTargetId())
                            && c.getStatus() == Status.PUBLISHED)
                    .sorted(Comparator.comparing(Comment::getCreatedAt))
                    .toList();
        }

        @Override
        public PagedResult<Comment> findPublishedByTarget(CommentTargetType targetType, String targetId, PageRequest pageRequest) {
            List<Comment> published = findPublishedByTarget(targetType, targetId);
            long total = published.size();
            PageRequest req = pageRequest != null ? pageRequest : PageRequest.first();
            int fromIndex = Math.min(req.getOffset(), published.size());
            int toIndex = Math.min(fromIndex + req.size(), published.size());
            List<Comment> pageItems = published.subList(fromIndex, toIndex);
            return PagedResult.of(pageItems, total, req);
        }

        @Override
        public List<Comment> findByAuthorId(AccountId authorId) {
            return db.values().stream()
                    .filter(c -> authorId.equals(c.getAuthorId()))
                    .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                    .toList();
        }

        @Override
        public List<Comment> findByContentIdAndAuthorId(ContentId contentId, AccountId authorId) {
            return db.values().stream()
                    .filter(c -> c.getTargetType() == CommentTargetType.CONTENT
                            && contentId.value().equals(c.getTargetId())
                            && authorId.equals(c.getAuthorId())
                            && c.getStatus() == Status.PUBLISHED)
                    .sorted(Comparator.comparing(Comment::getCreatedAt))
                    .toList();
        }

        @Override
        public long countByContentId(ContentId contentId) {
            return countPublishedByTarget(CommentTargetType.CONTENT, contentId.value());
        }

        @Override
        public long countPublishedByTarget(CommentTargetType targetType, String targetId) {
            return db.values().stream()
                    .filter(c -> c.getTargetType() == targetType
                            && targetId.equals(c.getTargetId())
                            && c.getStatus() == Status.PUBLISHED)
                    .count();
        }

        @Override
        public long countByAuthorIdSince(AccountId authorId, LocalDateTime since) {
            return db.values().stream()
                    .filter(c -> authorId.equals(c.getAuthorId())
                            && !c.getCreatedAt().isBefore(since))
                    .count();
        }

        @Override
        public void deleteById(CommentId id) {
            db.remove(id.value());
        }

        @Override
        public boolean existsById(CommentId id) {
            return db.containsKey(id.value());
        }
    }
}
