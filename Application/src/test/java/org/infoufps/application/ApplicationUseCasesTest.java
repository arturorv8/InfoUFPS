package org.infoufps.application;

import org.infoufps.domain.academic.MateriaSearchCriteria;
import org.infoufps.domain.academic.ProfesorSearchCriteria;
import org.infoufps.application.academic.MateriaDetailDto;
import org.infoufps.application.academic.ModeracionWarningDto;
import org.infoufps.application.academic.ProfesorDetailDto;
import org.infoufps.application.academic.*;
import org.infoufps.application.academic.ResenaDto;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountSearchCriteria;
import org.infoufps.domain.account.AccountStatus;
import org.infoufps.domain.account.IRefreshTokenRepository;
import org.infoufps.domain.account.RefreshToken;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.Resena;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.account.Role;
import org.infoufps.domain.academic.TipoDestino;
import org.infoufps.domain.academic.TipoMateria;
import org.infoufps.domain.shared.exceptions.DailyLimitExceededException;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.DuplicateResourceException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.application.academic.AdminCreateMateriaHandler;
import org.infoufps.application.academic.ProposeMateriaHandler;
import org.infoufps.application.academic.ModerateProfesorHandler;
import org.infoufps.application.academic.AdminCreateProfesorHandler;
import org.infoufps.application.academic.AdminUpdateProfesorHandler;
import org.infoufps.application.academic.AssociateMateriasToProfesorHandler;
import org.infoufps.application.academic.ProposeProfesorHandler;
import org.infoufps.application.academic.UpdateProposedProfesorHandler;
import org.infoufps.application.academic.CreateResenaHandler;
import org.infoufps.application.academic.DeleteResenaHandler;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.application.academic.DailyLimitService;
import org.infoufps.application.academic.SimilarityService;
import org.infoufps.domain.shared.time.BogotaTime;
import org.infoufps.domain.shared.text.TextNormalizer;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.MateriaId;
import org.infoufps.domain.academic.ProfesorId;
import org.infoufps.domain.academic.RatingSummary;
import org.infoufps.domain.academic.ResenaId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationUseCasesTest {

    private InMemoryProfesorRepository profesorRepo;
    private InMemoryMateriaRepository materiaRepo;
    private InMemoryResenaRepository resenaRepo;
    private InMemoryAccountRepository accountRepo;
    private DailyLimitService dailyLimitService;
    private SimilarityService similarityService;

    private ProposeProfesorHandler proposeProfesorHandler;
    private AdminCreateProfesorHandler adminCreateProfesorHandler;
    private ProposeMateriaHandler proposeMateriaHandler;
    private AdminCreateMateriaHandler adminCreateMateriaHandler;
    private CreateResenaHandler createResenaHandler;
    private DeleteResenaHandler deleteResenaHandler;
    private ModerateProfesorHandler moderateProfesorHandler;
    private org.infoufps.application.academic.ModerateMateriaHandler moderateMateriaHandler;
    private org.infoufps.application.academic.ModerateResenaHandler moderateResenaHandler;
    private org.infoufps.application.academic.UpdateProposedMateriaHandler updateProposedMateriaHandler;
    private org.infoufps.application.academic.UpdateResenaHandler updateResenaHandler;

    private Account testUser;

    @BeforeEach
    void setUp() {
        profesorRepo = new InMemoryProfesorRepository();
        materiaRepo = new InMemoryMateriaRepository();
        resenaRepo = new InMemoryResenaRepository();
        accountRepo = new InMemoryAccountRepository();

        testUser = Account.register("estudiante@ufps.edu.co", "hash123", "Estudiante UFPS", Role.USER);
        accountRepo.save(testUser);

        dailyLimitService = new DailyLimitService(profesorRepo, materiaRepo, resenaRepo);
        similarityService = new SimilarityService(profesorRepo, materiaRepo);

        proposeProfesorHandler = new ProposeProfesorHandler(profesorRepo, accountRepo, dailyLimitService);
        adminCreateProfesorHandler = new AdminCreateProfesorHandler(profesorRepo, materiaRepo);
        proposeMateriaHandler = new ProposeMateriaHandler(materiaRepo, accountRepo, dailyLimitService);
        adminCreateMateriaHandler = new AdminCreateMateriaHandler(materiaRepo);
        createResenaHandler = new CreateResenaHandler(resenaRepo, profesorRepo, materiaRepo, accountRepo, dailyLimitService);
        deleteResenaHandler = new DeleteResenaHandler(resenaRepo);
        moderateProfesorHandler = new ModerateProfesorHandler(profesorRepo, resenaRepo);
        moderateMateriaHandler = new org.infoufps.application.academic.ModerateMateriaHandler(materiaRepo, resenaRepo);
        moderateResenaHandler = new org.infoufps.application.academic.ModerateResenaHandler(resenaRepo, accountRepo);
        updateProposedMateriaHandler = new org.infoufps.application.academic.UpdateProposedMateriaHandler(materiaRepo);
        updateResenaHandler = new org.infoufps.application.academic.UpdateResenaHandler(resenaRepo, accountRepo);
    }

    @Test
    @DisplayName("No se puede duplicar un profesor publicado por nombre normalizado y facultad")
    void testCannotDuplicatePublishedProfesor() {
        // Admin crea profesor en Ciencias Básicas
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Pérez, Juan Carlos", Facultad.CIENCIAS_BASICAS, "juan.perez@ufps.edu.co", Set.of()));

        // Usuario intenta proponer profesor con orden de palabras desordenado y tildes en la misma facultad
        ProposeProfesorCommand dupCommand = new ProposeProfesorCommand("Juan Carlos Perez", Facultad.CIENCIAS_BASICAS, null, testUser.getAccountId().value());

        assertThrows(DuplicateResourceException.class, () -> proposeProfesorHandler.execute(dupCommand));

        // Pero en OTRA facultad sí es permitido
        assertDoesNotThrow(() ->
                proposeProfesorHandler.execute(new ProposeProfesorCommand("Juan Carlos Perez", Facultad.INGENIERIA, null, testUser.getAccountId().value())));
    }

    @Test
    @DisplayName("Validación de email único para profesores")
    void testUniqueProfesorEmail() {
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Profesor Uno", Facultad.INGENIERIA, "repetido@ufps.edu.co", Set.of()));

        // Intentar registrar otro profesor con el mismo email (incluso con distinto case) debe fallar
        ProposeProfesorCommand dupEmail = new ProposeProfesorCommand("Profesor Dos", Facultad.CIENCIAS_DE_LA_SALUD, "REPETIDO@ufps.edu.co", testUser.getAccountId().value());
        assertThrows(DuplicateResourceException.class, () -> proposeProfesorHandler.execute(dupEmail));
    }

    @Test
    @DisplayName("Materias: código único cuando existe")
    void testUniqueMateriaCodigo() {
        adminCreateMateriaHandler.execute(new AdminSaveMateriaCommand("Cálculo I", "1150101", 4, TipoMateria.CARRERA));

        // Proponer materia con el mismo código debe fallar
        ProposeMateriaCommand dupCode = new ProposeMateriaCommand("Cálculo Diferencial", "1150101", 3, TipoMateria.CARRERA, testUser.getAccountId().value());
        assertThrows(DuplicateResourceException.class, () -> proposeMateriaHandler.execute(dupCode));
    }

    @Test
    @DisplayName("Unicidad de reseñas activas por usuario y destino")
    void testActiveReviewUniqueness() {
        // Crear profesor publicado
        ProfesorDetailDto prof = adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Profesor Titular", Facultad.INGENIERIA, null, Set.of()));

        // Crear primera reseña
        CreateResenaCommand cmd1 = new CreateResenaCommand(testUser.getAccountId().value(), TipoDestino.PROFESOR, prof.id(), null, 5, "Excelente", "BD");
        createResenaHandler.execute(cmd1);

        // Intentar crear segunda reseña mientras la primera esté activa debe fallar
        CreateResenaCommand cmd2 = new CreateResenaCommand(testUser.getAccountId().value(), TipoDestino.PROFESOR, prof.id(), null, 4, "Otra opinión", "BD");
        assertThrows(DuplicateResourceException.class, () -> createResenaHandler.execute(cmd2));

        // Si el usuario elimina (archiva) su reseña anterior, ahora sí puede crear una nueva
        Resena primera = resenaRepo.findActiveByUserAndProfesor(testUser.getAccountId(), ProfesorId.of(prof.id())).orElseThrow();
        deleteResenaHandler.execute(primera.getId().value(), testUser.getAccountId().value(), false);

        assertDoesNotThrow(() -> createResenaHandler.execute(cmd2));
    }

    @Test
    @DisplayName("Promedios y conteos solo se calculan sobre reseñas publicadas")
    void testAverageRatingOnlyPublished() {
        ProfesorDetailDto prof = adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Docente Evaluado", Facultad.INGENIERIA, null, Set.of()));
        ProfesorId profId = ProfesorId.of(prof.id());

        // Reseña 1: en revisión (calif 5) -> promedio no debe contarla
        Resena r1 = Resena.createForProfesor(testUser.getAccountId(), profId, 5, "Comentario 1", null);
        resenaRepo.save(r1);
        assertNull(resenaRepo.getAverageRatingForProfesor(profId));
        assertEquals(0, resenaRepo.countPublishedByProfesor(profId));

        // Publicar reseña 1
        r1.publish();
        resenaRepo.save(r1);
        assertEquals(5.0, resenaRepo.getAverageRatingForProfesor(profId));
        assertEquals(1, resenaRepo.countPublishedByProfesor(profId));

        // Reseña 2: publicada (calif 3)
        Resena r2 = Resena.createForProfesor(AccountId.generate(), profId, 3, "Comentario 2", null);
        r2.publish();
        resenaRepo.save(r2);
        assertEquals(4.0, resenaRepo.getAverageRatingForProfesor(profId));
        assertEquals(2, resenaRepo.countPublishedByProfesor(profId));

        // Reseña 3: archivada (calif 1) -> no debe afectar el promedio
        Resena r3 = Resena.createForProfesor(AccountId.generate(), profId, 1, "Comentario 3", null);
        r3.archive();
        resenaRepo.save(r3);
        assertEquals(4.0, resenaRepo.getAverageRatingForProfesor(profId));
        assertEquals(2, resenaRepo.countPublishedByProfesor(profId));
    }

    @Test
    @DisplayName("Límite diario de propuestas")
    void testDailyLimitEnforcement() {
        // Enviar propuestas hasta el límite diario (10)
        for (int i = 1; i <= DailyLimitService.MAX_DAILY_PROFESOR_PROPOSALS; i++) {
            proposeProfesorHandler.execute(new ProposeProfesorCommand("Profesor Número " + i, Facultad.INGENIERIA, null, testUser.getAccountId().value()));
        }

        // La 11va debe ser rechazada con DailyLimitExceededException
        ProposeProfesorCommand extra = new ProposeProfesorCommand("Profesor Extra", Facultad.INGENIERIA, null, testUser.getAccountId().value());
        assertThrows(DailyLimitExceededException.class, () -> proposeProfesorHandler.execute(extra));
    }

    @Test
    @DisplayName("Detección de similitud advierte al admin sobre nombres similares")
    void testSimilarityDetection() {
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Roberto Carlos Gómez", Facultad.INGENIERIA, null, Set.of()));

        List<ModeracionWarningDto> warnings = similarityService.checkProfesorSimilarity("Roberto C. Gómez", Facultad.INGENIERIA);
        assertFalse(warnings.isEmpty(), "Debe encontrar coincidencia con similitud >= 70%");
        assertTrue(warnings.get(0).similitud() >= 0.70);
    }

    @Test
    @DisplayName("Moderación: publicar con correcciones de admin")
    void testModerateWithCorrections() {
        ProfesorDetailDto proposal = proposeProfesorHandler.execute(
                new ProposeProfesorCommand("Profe Con Errores", Facultad.NO_ESPECIFICADA, null, testUser.getAccountId().value()));

        ModerateEntityCommand correction = new ModerateEntityCommand("Profesor Corregido", Facultad.INGENIERIA, "corregido@ufps.edu.co", null, null, null);
        ProfesorDetailDto published = moderateProfesorHandler.publish(proposal.id(), correction);

        assertEquals("Profesor Corregido", published.nombre());
        assertEquals(Facultad.INGENIERIA, published.facultad());
        assertEquals("corregido@ufps.edu.co", published.email());
        assertEquals(EstadoModeracion.PUBLICADO, published.estado());
    }

    @Test
    @DisplayName("Moderación: publicar sin correcciones valida nombre duplicado contra publicados")
    void testPublishWithoutCorrectionsRejectsDuplicateName() {
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Carlos Andrés Pérez", Facultad.INGENIERIA, null, Set.of()));

        Profesor proposal = Profesor.propose("Perez Carlos Andres", Facultad.INGENIERIA, null, testUser.getAccountId());
        profesorRepo.save(proposal);

        assertThrows(DuplicateResourceException.class,
                () -> moderateProfesorHandler.publish(proposal.getId().value(), null));
    }

    @Test
    @DisplayName("Moderación: publicar sin correcciones valida email duplicado excluyendo el propio")
    void testPublishWithoutCorrectionsRejectsDuplicateEmail() {
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Profesor Existente", Facultad.INGENIERIA, "ocupado@ufps.edu.co", Set.of()));

        Profesor proposal = Profesor.propose("Otro Profesor", Facultad.CIENCIAS_BASICAS, "ocupado@ufps.edu.co", testUser.getAccountId());
        profesorRepo.save(proposal);

        assertThrows(DuplicateResourceException.class,
                () -> moderateProfesorHandler.publish(proposal.getId().value(), null));
    }

    @Test
    @DisplayName("Moderación: publicar sin correcciones permite datos únicos")
    void testPublishWithoutCorrectionsSucceedsWhenUnique() {
        Profesor proposal = Profesor.propose("Profesor Único", Facultad.CIENCIAS_BASICAS, "unico@ufps.edu.co", testUser.getAccountId());
        profesorRepo.save(proposal);

        ProfesorDetailDto published = moderateProfesorHandler.publish(proposal.getId().value(), null);

        assertEquals(EstadoModeracion.PUBLICADO, published.estado());
        assertEquals("Profesor Único", published.nombre());
    }

    @Test
    @DisplayName("Editar propuesta sin facultad conserva la facultad actual al validar duplicados")
    void testUpdateProposalUsesCurrentFacultyForDuplicateValidation() {
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Profesor Base", Facultad.NO_ESPECIFICADA, null, Set.of()));
        ProfesorDetailDto propuesta = proposeProfesorHandler.execute(
                new ProposeProfesorCommand("Profesor Otro", Facultad.INGENIERIA, null, testUser.getAccountId().value()));

        UpdateProposedProfesorHandler updateHandler = new UpdateProposedProfesorHandler(profesorRepo);
        ProfesorDetailDto updated = updateHandler.execute(new UpdateProposedProfesorCommand(
                propuesta.id(), "Profesor Base", null, null, testUser.getAccountId().value()));

        assertEquals("Profesor Base", updated.nombre());
        assertEquals(Facultad.INGENIERIA, updated.facultad(),
                "Debe conservar la facultad actual y no comparar contra NO_ESPECIFICADA");
    }

    @Test
    @DisplayName("Editar como admin sin facultad conserva la facultad actual al validar duplicados")
    void testAdminUpdateUsesCurrentFacultyForDuplicateValidation() {
        adminCreateProfesorHandler.execute(new AdminSaveProfesorCommand("Profesor Base", Facultad.NO_ESPECIFICADA, null, Set.of()));
        ProfesorDetailDto otro = adminCreateProfesorHandler.execute(
                new AdminSaveProfesorCommand("Profesor Otro", Facultad.INGENIERIA, null, Set.of()));

        AdminUpdateProfesorHandler updateHandler = new AdminUpdateProfesorHandler(profesorRepo, materiaRepo, resenaRepo);
        ProfesorDetailDto updated = updateHandler.execute(
                otro.id(), new AdminSaveProfesorCommand("Profesor Base", null, null, null));

        assertEquals("Profesor Base", updated.nombre());
        assertEquals(Facultad.INGENIERIA, updated.facultad(),
                "Debe conservar la facultad actual y no comparar contra NO_ESPECIFICADA");
    }

    @Test
    @DisplayName("Crear profesor rechaza materias inexistentes")
    void testAdminCreateProfesorRejectsUnknownMaterias() {
        assertThrows(NotFoundException.class, () -> adminCreateProfesorHandler.execute(
                new AdminSaveProfesorCommand("Profesor Materias", Facultad.INGENIERIA, null, Set.of("no-existe"))));
    }

    @Test
    @DisplayName("Crear profesor rechaza materias no publicadas")
    void testAdminCreateProfesorRejectsUnpublishedMaterias() {
        Materia propuesta = Materia.propose("Materia En Revisión", "9999999", 3, TipoMateria.CARRERA, testUser.getAccountId());
        materiaRepo.save(propuesta);

        assertThrows(DomainException.class, () -> adminCreateProfesorHandler.execute(
                new AdminSaveProfesorCommand("Profesor Materias", Facultad.INGENIERIA, null, Set.of(propuesta.getId().value()))));
    }

    @Test
    @DisplayName("Actualizar profesor rechaza materias inexistentes y no publicadas")
    void testAdminUpdateProfesorRejectsInvalidMaterias() {
        ProfesorDetailDto profesor = adminCreateProfesorHandler.execute(
                new AdminSaveProfesorCommand("Profesor Update", Facultad.INGENIERIA, null, Set.of()));
        Materia propuesta = Materia.propose("Materia En Revisión", "9999998", 3, TipoMateria.CARRERA, testUser.getAccountId());
        materiaRepo.save(propuesta);
        AdminUpdateProfesorHandler updateHandler = new AdminUpdateProfesorHandler(profesorRepo, materiaRepo, resenaRepo);

        assertThrows(NotFoundException.class, () -> updateHandler.execute(profesor.id(),
                new AdminSaveProfesorCommand("Profesor Update", Facultad.INGENIERIA, null, Set.of("no-existe"))));
        assertThrows(DomainException.class, () -> updateHandler.execute(profesor.id(),
                new AdminSaveProfesorCommand("Profesor Update", Facultad.INGENIERIA, null, Set.of(propuesta.getId().value()))));
    }

    @Test
    @DisplayName("Asociar materias rechaza materias inexistentes y no publicadas")
    void testAssociateMateriasRejectsInvalidMaterias() {
        ProfesorDetailDto profesor = adminCreateProfesorHandler.execute(
                new AdminSaveProfesorCommand("Profesor Asociado", Facultad.INGENIERIA, null, Set.of()));
        Materia propuesta = Materia.propose("Materia En Revisión", "9999997", 3, TipoMateria.CARRERA, testUser.getAccountId());
        materiaRepo.save(propuesta);
        AssociateMateriasToProfesorHandler associateHandler =
                new AssociateMateriasToProfesorHandler(profesorRepo, materiaRepo, resenaRepo);

        assertThrows(NotFoundException.class,
                () -> associateHandler.execute(profesor.id(), Set.of("no-existe")));
        assertThrows(DomainException.class,
                () -> associateHandler.execute(profesor.id(), Set.of(propuesta.getId().value())));
    }

    @Test
    @DisplayName("Eliminar una reseña ya archivada es idempotente")
    void testDeleteResenaIsIdempotent() {
        ProfesorDetailDto profesor = adminCreateProfesorHandler.execute(
                new AdminSaveProfesorCommand("Profesor Reseña", Facultad.INGENIERIA, null, Set.of()));
        Resena resena = Resena.createForProfesor(testUser.getAccountId(), ProfesorId.of(profesor.id()), 5, "Buena", null);
        resena.archive();
        resenaRepo.save(resena);

        assertDoesNotThrow(() -> deleteResenaHandler.execute(
                resena.getId().value(), testUser.getAccountId().value(), false));
        assertEquals(EstadoModeracion.ARCHIVADO, resenaRepo.findById(resena.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("Paginación real en repositorios en memoria")
    void testInMemoryPagination() {
        for (int i = 1; i <= 15; i++) {
            materiaRepo.save(Materia.createByAdmin("Materia " + String.format("%02d", i), "MAT" + i, 3, org.infoufps.domain.academic.TipoMateria.CARRERA));
        }
        PageResult<Materia> page1 = materiaRepo.search(
                new org.infoufps.domain.academic.MateriaSearchCriteria(null, null, null, null), PageRequest.of(1, 10));
        assertEquals(10, page1.items().size());
        assertEquals(15, page1.total());
        assertTrue(page1.hasNext());

        PageResult<Materia> page2 = materiaRepo.search(
                new org.infoufps.domain.academic.MateriaSearchCriteria(null, null, null, null), PageRequest.of(2, 10));
        assertEquals(5, page2.items().size());
        assertEquals(15, page2.total());
        assertFalse(page2.hasNext());
    }

    @Test
    @DisplayName("Moderación de materias: publicar con y sin correcciones, archivar")
    void testModerateMateriaWorkflow() {
        Materia prop = Materia.propose("Estructuras de Datos", "1150201", 4, TipoMateria.CARRERA, testUser.getAccountId());
        materiaRepo.save(prop);

        // Archivar propuesta
        Materia propToArchive = Materia.propose("Materia Descartada", "1150202", 3, TipoMateria.ELECTIVA, testUser.getAccountId());
        materiaRepo.save(propToArchive);
        MateriaDetailDto archived = moderateMateriaHandler.archive(propToArchive.getId().value());
        assertEquals(EstadoModeracion.ARCHIVADO, archived.estado());

        // Publicar con correcciones
        ModerateEntityCommand corr = new ModerateEntityCommand("Estructuras de Datos Avanzadas", null, null, "1150209", 4, TipoMateria.ELECTIVA);
        MateriaDetailDto published = moderateMateriaHandler.publish(prop.getId().value(), corr);
        assertEquals(EstadoModeracion.PUBLICADO, published.estado());
        assertEquals("Estructuras de Datos Avanzadas", published.nombre());
        assertEquals("1150209", published.codigo());
        assertEquals(TipoMateria.ELECTIVA, published.tipo());
    }

    @Test
    @DisplayName("Moderación de profesores: archivar propuesta")
    void testModerateProfesorArchive() {
        Profesor proposal = Profesor.propose("Profesor A Descartar", Facultad.INGENIERIA, null, testUser.getAccountId());
        profesorRepo.save(proposal);

        ProfesorDetailDto archived = moderateProfesorHandler.archive(proposal.getId().value());
        assertEquals(EstadoModeracion.ARCHIVADO, archived.estado());
    }

    @Test
    @DisplayName("Moderación de reseñas: publicar y archivar")
    void testModerateResenaWorkflow() {
        ProfesorDetailDto prof = adminCreateProfesorHandler.execute(
                new AdminSaveProfesorCommand("Docente Moderado", Facultad.INGENIERIA, null, Set.of()));
        Resena resena = Resena.createForProfesor(testUser.getAccountId(), ProfesorId.of(prof.id()), 4, "Buena clase", null);
        resenaRepo.save(resena);

        // Publicar
        ResenaDto published = moderateResenaHandler.publish(resena.getId().value());
        assertEquals(EstadoModeracion.PUBLICADO, published.estado());

        // Archivar
        ResenaDto archived = moderateResenaHandler.archive(resena.getId().value());
        assertEquals(EstadoModeracion.ARCHIVADO, archived.estado());
    }

    @Test
    @DisplayName("Actualizar propuesta de materia por el proponente")
    void testUpdateProposedMateria() {
        Materia prop = Materia.propose("Física Mecánica", "1150301", 4, TipoMateria.CARRERA, testUser.getAccountId());
        materiaRepo.save(prop);

        // Actualización válida
        org.infoufps.application.academic.UpdateProposedMateriaCommand cmd =
                new org.infoufps.application.academic.UpdateProposedMateriaCommand(
                        prop.getId().value(), "Física I", "1150302", 3, TipoMateria.ELECTIVA, testUser.getAccountId().value());
        MateriaDetailDto updated = updateProposedMateriaHandler.execute(cmd);
        assertEquals("Física I", updated.nombre());
        assertEquals("1150302", updated.codigo());
        assertEquals(3, updated.creditos());

        // Intento de edición por otro usuario -> ForbiddenException
        AccountId stranger = AccountId.generate();
        org.infoufps.application.academic.UpdateProposedMateriaCommand forbiddenCmd =
                new org.infoufps.application.academic.UpdateProposedMateriaCommand(
                        prop.getId().value(), "Hack", "1150399", 3, TipoMateria.ELECTIVA, stranger.value());
        assertThrows(ForbiddenException.class, () -> updateProposedMateriaHandler.execute(forbiddenCmd));

        // Intento de edición tras publicar -> DomainException
        prop.publish();
        materiaRepo.save(prop);
        assertThrows(DomainException.class, () -> updateProposedMateriaHandler.execute(cmd));
    }

    @Test
    @DisplayName("Actualizar reseña por su autor")
    void testUpdateResena() {
        ProfesorDetailDto prof = adminCreateProfesorHandler.execute(
                new AdminSaveProfesorCommand("Docente Reseñado", Facultad.INGENIERIA, null, Set.of()));
        Resena resena = Resena.createForProfesor(testUser.getAccountId(), ProfesorId.of(prof.id()), 3, "Regular", null);
        resenaRepo.save(resena);

        // Autor actualiza su reseña
        org.infoufps.application.academic.UpdateResenaCommand cmd =
                new org.infoufps.application.academic.UpdateResenaCommand(
                        resena.getId().value(), testUser.getAccountId().value(), 5, "Excelente profesor, cambió mi opinión", "Cálculo");
        ResenaDto updated = updateResenaHandler.execute(cmd);
        assertEquals(5, updated.calificacion());
        assertEquals("Excelente profesor, cambió mi opinión", updated.comentario());

        // Otro usuario intenta actualizar -> ForbiddenException
        org.infoufps.application.academic.UpdateResenaCommand forbiddenCmd =
                new org.infoufps.application.academic.UpdateResenaCommand(
                        resena.getId().value(), AccountId.generate().value(), 1, "Hacked", null);
        assertThrows(ForbiddenException.class, () -> updateResenaHandler.execute(forbiddenCmd));
    }

    @Test
    @DisplayName("Materia.adminUpdate actualiza de forma consistente tipo, código y créditos")
    void testMateriaAdminUpdate() {
        Materia m = Materia.createByAdmin("Química", "1150401", 3, TipoMateria.CARRERA);
        m.adminUpdate("Química Orgánica", "1150402", 4, TipoMateria.ELECTIVA);

        assertEquals("Química Orgánica", m.getNombre());
        assertEquals("1150402", m.getCodigo());
        assertEquals(4, m.getCreditos());
        assertEquals(TipoMateria.ELECTIVA, m.getTipo());
    }

    @Test
    @DisplayName("GetCatalogosQuery excluye NO_ESPECIFICADA del catálogo público")
    void testCatalogosExcludesNoEspecificada() {
        org.infoufps.application.shared.GetCatalogosQuery query = new org.infoufps.application.shared.GetCatalogosQuery();
        var facultades = query.getFacultades();
        assertTrue(facultades.stream().noneMatch(f -> "NO_ESPECIFICADA".equals(f.clave())));
        assertTrue(facultades.stream().anyMatch(f -> "INGENIERIA".equals(f.clave())));
    }

    // ─────────────────────────────────────────────────────────────────────
    // Repositorios en memoria para pruebas rápidas y deterministas
    // ─────────────────────────────────────────────────────────────────────

    public static class InMemoryProfesorRepository implements IProfesorRepository {
        Map<String, Profesor> db = new HashMap<>();

        @Override public Profesor save(Profesor p) { db.put(p.getId().value(), p); return p; }
        @Override public Optional<Profesor> findById(ProfesorId id) { return Optional.ofNullable(db.get(id.value())); }
        @Override
        public PageResult<Profesor> search(ProfesorSearchCriteria criteria, PageRequest pr) {
            List<Profesor> list = db.values().stream()
                    .filter(p -> criteria.estado() == null || p.getEstado() == criteria.estado())
                    .filter(p -> criteria.facultad() == null || p.getFacultad() == criteria.facultad())
                    .filter(p -> criteria.query() == null || TextNormalizer.matchesQueryTokens(p.getNombre(), criteria.query()))
                    .sorted(Comparator.comparing(Profesor::getNombre))
                    .toList();
            return paginate(list, pr);
        }
        @Override
        public boolean existsByNombreNormalizadoAndFacultadAndEstado(String norm, Facultad fac, EstadoModeracion est) {
            return db.values().stream().anyMatch(p -> p.getNombreNormalizado().equals(norm) && p.getFacultad() == fac && p.getEstado() == est);
        }
        @Override
        public boolean existsByEmail(String email) {
            return db.values().stream().anyMatch(p -> p.getEmail() != null && p.getEmail().equalsIgnoreCase(email.trim()));
        }
        @Override
        public boolean existsByEmailAndNotId(String email, ProfesorId excludeId) {
            return db.values().stream().anyMatch(p -> !p.getId().equals(excludeId) && p.getEmail() != null && p.getEmail().equalsIgnoreCase(email.trim()));
        }
        @Override
        public List<Profesor> findSimilar(String norm, Facultad fac) {
            return db.values().stream()
                    .filter(p -> p.getEstado() != EstadoModeracion.ARCHIVADO)
                    .filter(p -> fac == null || fac == Facultad.NO_ESPECIFICADA || p.getFacultad() == fac)
                    .toList();
        }
        @Override
        public List<Profesor> findByProponenteId(AccountId pid) {
            return db.values().stream().filter(p -> pid.equals(p.getProponenteId())).toList();
        }
        @Override
        public long countTodaySubmissionsByProponente(AccountId pid) {
            return db.values().stream()
                    .filter(p -> pid.equals(p.getProponenteId()) && p.getCreatedAt().toLocalDate().isEqual(BogotaTime.today()))
                    .count();
        }
        @Override public void deleteById(ProfesorId id) { db.remove(id.value()); }
    }

    public static class InMemoryMateriaRepository implements IMateriaRepository {
        Map<String, Materia> db = new HashMap<>();

        @Override public Materia save(Materia m) { db.put(m.getId().value(), m); return m; }
        @Override public Optional<Materia> findById(MateriaId id) { return Optional.ofNullable(db.get(id.value())); }
        @Override
        public Optional<Materia> findByCodigo(String code) {
            return db.values().stream().filter(m -> m.getCodigo() != null && m.getCodigo().equalsIgnoreCase(code.trim())).findFirst();
        }
        @Override
        public PageResult<Materia> search(MateriaSearchCriteria criteria, PageRequest pr) {
            List<Materia> list = db.values().stream()
                    .filter(m -> criteria.estado() == null || m.getEstado() == criteria.estado())
                    .filter(m -> criteria.creditos() == null || Objects.equals(m.getCreditos(), criteria.creditos()))
                    .filter(m -> criteria.tipo() == null || m.getTipo() == criteria.tipo())
                    .filter(m -> criteria.query() == null || (m.getCodigo() != null && m.getCodigo().equalsIgnoreCase(criteria.query().trim()))
                            || TextNormalizer.matchesQueryTokens(m.getNombre(), criteria.query()))
                    .sorted(Comparator.comparing(Materia::getNombre))
                    .toList();
            return paginate(list, pr);
        }
        @Override
        public boolean existsByCodigo(String code) {
            return db.values().stream().anyMatch(m -> m.getCodigo() != null && m.getCodigo().equalsIgnoreCase(code.trim()));
        }
        @Override
        public boolean existsByCodigoAndNotId(String code, MateriaId excludeId) {
            return db.values().stream().anyMatch(m -> !m.getId().equals(excludeId) && m.getCodigo() != null && m.getCodigo().equalsIgnoreCase(code.trim()));
        }
        @Override
        public List<Materia> findSimilar() {
            return db.values().stream().filter(m -> m.getEstado() != EstadoModeracion.ARCHIVADO).toList();
        }
        @Override
        public List<Materia> findByProponenteId(AccountId pid) {
            return db.values().stream().filter(m -> pid.equals(m.getProponenteId())).toList();
        }
        @Override
        public long countTodaySubmissionsByProponente(AccountId pid) {
            return db.values().stream()
                    .filter(m -> pid.equals(m.getProponenteId()) && m.getCreatedAt().toLocalDate().isEqual(BogotaTime.today()))
                    .count();
        }
        @Override
        public List<Materia> findByIds(Collection<MateriaId> ids) {
            return db.values().stream().filter(m -> ids.contains(m.getId())).toList();
        }
        @Override public void deleteById(MateriaId id) { db.remove(id.value()); }
    }

    public static class InMemoryResenaRepository implements IResenaRepository {
        Map<String, Resena> db = new HashMap<>();

        @Override public Resena save(Resena r) { db.put(r.getId().value(), r); return r; }
        @Override public Optional<Resena> findById(ResenaId id) { return Optional.ofNullable(db.get(id.value())); }
        @Override
        public Optional<Resena> findActiveByUserAndProfesor(AccountId aid, ProfesorId pid) {
            return db.values().stream()
                    .filter(r -> r.getAutorId().equals(aid) && pid.equals(r.getProfesorId()) && r.getEstado() != EstadoModeracion.ARCHIVADO)
                    .findFirst();
        }
        @Override
        public Optional<Resena> findActiveByUserAndMateria(AccountId aid, MateriaId mid) {
            return db.values().stream()
                    .filter(r -> r.getAutorId().equals(aid) && mid.equals(r.getMateriaId()) && r.getEstado() != EstadoModeracion.ARCHIVADO)
                    .findFirst();
        }
        @Override
        public PageResult<Resena> findPublishedByProfesor(ProfesorId pid, PageRequest pr) {
            List<Resena> list = db.values().stream()
                    .filter(r -> pid.equals(r.getProfesorId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                    .sorted(Comparator.comparing(Resena::getCreatedAt).reversed())
                    .toList();
            return paginate(list, pr);
        }
        @Override
        public PageResult<Resena> findPublishedByMateria(MateriaId mid, PageRequest pr) {
            List<Resena> list = db.values().stream()
                    .filter(r -> mid.equals(r.getMateriaId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                    .sorted(Comparator.comparing(Resena::getCreatedAt).reversed())
                    .toList();
            return paginate(list, pr);
        }
        @Override
        public List<Resena> findByAutorId(AccountId aid) {
            return db.values().stream().filter(r -> r.getAutorId().equals(aid)).toList();
        }
        @Override
        public PageResult<Resena> searchModeration(TipoDestino d, EstadoModeracion e, PageRequest pr) {
            List<Resena> list = db.values().stream()
                    .filter(r -> d == null || r.getTipoDestino() == d)
                    .filter(r -> e == null || r.getEstado() == e)
                    .toList();
            return paginate(list, pr);
        }
        @Override
        public Double getAverageRatingForProfesor(ProfesorId pid) {
            List<Integer> ratings = db.values().stream()
                    .filter(r -> pid.equals(r.getProfesorId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                    .map(Resena::getCalificacion)
                    .toList();
            return ratings.isEmpty() ? null : ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        }
        @Override
        public long countPublishedByProfesor(ProfesorId pid) {
            return db.values().stream()
                    .filter(r -> pid.equals(r.getProfesorId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                    .count();
        }
        @Override
        public Double getAverageRatingForMateria(MateriaId mid) {
            List<Integer> ratings = db.values().stream()
                    .filter(r -> mid.equals(r.getMateriaId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                    .map(Resena::getCalificacion)
                    .toList();
            return ratings.isEmpty() ? null : ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        }
        @Override
        public long countPublishedByMateria(MateriaId mid) {
            return db.values().stream()
                    .filter(r -> mid.equals(r.getMateriaId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                    .count();
        }
        @Override
        public Map<String, RatingSummary> getRatingSummariesByProfesor(Collection<ProfesorId> ids) {
            Map<String, RatingSummary> summaries = new HashMap<>();
            for (ProfesorId pid : ids) {
                List<Integer> ratings = db.values().stream()
                        .filter(r -> pid.equals(r.getProfesorId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                        .map(Resena::getCalificacion)
                        .toList();
                if (!ratings.isEmpty()) {
                    summaries.put(pid.value(), new RatingSummary(
                            ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0),
                            ratings.size()));
                }
            }
            return summaries;
        }
        @Override
        public Map<String, RatingSummary> getRatingSummariesByMateria(Collection<MateriaId> ids) {
            Map<String, RatingSummary> summaries = new HashMap<>();
            for (MateriaId mid : ids) {
                List<Integer> ratings = db.values().stream()
                        .filter(r -> mid.equals(r.getMateriaId()) && r.getEstado() == EstadoModeracion.PUBLICADO)
                        .map(Resena::getCalificacion)
                        .toList();
                if (!ratings.isEmpty()) {
                    summaries.put(mid.value(), new RatingSummary(
                            ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0),
                            ratings.size()));
                }
            }
            return summaries;
        }
        @Override
        public long countTodaySubmissionsByAutor(AccountId aid) {
            return db.values().stream()
                    .filter(r -> aid.equals(r.getAutorId()) && r.getCreatedAt().toLocalDate().isEqual(BogotaTime.today()))
                    .count();
        }
        @Override public void deleteById(ResenaId id) { db.remove(id.value()); }
    }

    public static class InMemoryAccountRepository implements IAccountRepository {
        Map<String, Account> db = new HashMap<>();

        @Override public Account save(Account a) { db.put(a.getAccountId().value(), a); return a; }
        @Override public Optional<Account> findById(AccountId id) { return Optional.ofNullable(db.get(id.value())); }
        @Override
        public Optional<Account> findByEmail(String email) {
            return db.values().stream().filter(a -> a.getEmail().equalsIgnoreCase(email)).findFirst();
        }
        @Override
        public boolean existsByEmail(String email) {
            return db.values().stream().anyMatch(a -> a.getEmail().equalsIgnoreCase(email));
        }
        @Override
        public List<Account> findByIds(Collection<AccountId> ids) {
            return ids.stream()
                    .map(id -> db.get(id.value()))
                    .filter(Objects::nonNull)
                    .toList();
        }
        @Override
        public PageResult<Account> search(AccountSearchCriteria criteria, PageRequest pageRequest) {
            AccountSearchCriteria c = criteria != null
                    ? criteria
                    : new AccountSearchCriteria(null, null, null);
            String query = c.query() != null ? c.query().toLowerCase(Locale.ROOT) : null;
            List<Account> filtered = db.values().stream()
                    .filter(a -> c.role() == null || a.getRole() == c.role())
                    .filter(a -> c.status() == null || a.getStatus() == c.status())
                    .filter(a -> query == null
                            || a.getName().toLowerCase(Locale.ROOT).contains(query)
                            || a.getEmail().toLowerCase(Locale.ROOT).contains(query))
                    .sorted(Comparator.comparing(a -> a.getName().toLowerCase(Locale.ROOT)))
                    .toList();
            return paginate(filtered, pageRequest);
        }
        @Override
        public long countActiveAdminsExcluding(AccountId accountId) {
            return db.values().stream()
                    .filter(a -> !a.getAccountId().equals(accountId))
                    .filter(a -> a.getRole() == Role.ADMIN && a.getStatus() == AccountStatus.ACTIVE)
                    .count();
        }
        @Override public void deleteById(AccountId id) { db.remove(id.value()); }
    }

    public static class InMemoryRefreshTokenRepository implements IRefreshTokenRepository {
        Map<String, RefreshToken> db = new HashMap<>();

        @Override
        public RefreshToken save(RefreshToken token) {
            db.put(token.getTokenId(), token);
            return token;
        }

        @Override
        public Optional<RefreshToken> findByTokenHash(String tokenHash) {
            return db.values().stream()
                    .filter(t -> t.getTokenHash().equals(tokenHash))
                    .findFirst();
        }

        @Override
        public boolean revokeIfActive(String tokenId, java.time.LocalDateTime revokedAt) {
            RefreshToken token = db.get(tokenId);
            if (token == null || token.isRevoked()) {
                return false;
            }
            token.revoke(revokedAt);
            return true;
        }

        @Override
        public void revokeAllByAccountId(AccountId accountId, java.time.LocalDateTime revokedAt) {
            db.values().stream()
                    .filter(t -> t.getAccountId().equals(accountId))
                    .filter(t -> !t.isRevoked())
                    .forEach(t -> t.revoke(revokedAt));
        }

        public List<RefreshToken> tokensOf(AccountId accountId) {
            return db.values().stream()
                    .filter(t -> t.getAccountId().equals(accountId))
                    .toList();
        }
    }

    private static <T> PageResult<T> paginate(List<T> all, PageRequest pr) {
        PageRequest req = pr != null ? pr : PageRequest.first();
        int from = Math.min(req.getOffset(), all.size());
        int to = Math.min(from + req.size(), all.size());
        return PageResult.of(new ArrayList<>(all.subList(from, to)), all.size(), req);
    }
}

