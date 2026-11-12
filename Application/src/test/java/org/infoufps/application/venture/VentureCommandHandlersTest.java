package org.infoufps.application.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.ConflictException;
import org.infoufps.domain.shared.exceptions.DailyLimitExceededException;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.venture.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Casos de Uso y Handlers de Comandos de Venture")
class VentureCommandHandlersTest {

    private InMemoryVentureRepository ventureRepository;
    private InMemoryVentureRatingRepository ratingRepository;
    private InMemoryFavoriteVentureRepository favoriteRepository;
    private InMemoryVentureReportRepository reportRepository;
    private TestVentureSettings settings;

    private CreateVentureHandler createHandler;
    private UpdateVentureHandler updateHandler;
    private ActivateVentureHandler activateHandler;
    private DeactivateVentureHandler deactivateHandler;
    private PublishVentureHandler publishHandler;
    private ArchiveVentureHandler archiveHandler;
    private RateVentureHandler rateHandler;
    private DeleteRatingHandler deleteRatingHandler;
    private AddFavoriteHandler addFavoriteHandler;
    private RemoveFavoriteHandler removeFavoriteHandler;
    private ReportVentureHandler reportHandler;
    private ResolveVentureReportHandler resolveReportHandler;

    private final String OWNER = "owner-123";
    private final String OTHER_USER = "other-456";

    @BeforeEach
    void setUp() {
        ventureRepository = new InMemoryVentureRepository();
        ratingRepository = new InMemoryVentureRatingRepository();
        favoriteRepository = new InMemoryFavoriteVentureRepository(ventureRepository::findById);
        reportRepository = new InMemoryVentureReportRepository();
        settings = new TestVentureSettings();

        ventureRepository.setRatingProvider(ratingRepository::getRatingSummary);

        createHandler = new CreateVentureHandler(ventureRepository, settings);
        updateHandler = new UpdateVentureHandler(ventureRepository, ratingRepository);
        activateHandler = new ActivateVentureHandler(ventureRepository);
        deactivateHandler = new DeactivateVentureHandler(ventureRepository);
        publishHandler = new PublishVentureHandler(ventureRepository);
        archiveHandler = new ArchiveVentureHandler(ventureRepository);
        rateHandler = new RateVentureHandler(ventureRepository, ratingRepository);
        deleteRatingHandler = new DeleteRatingHandler(ratingRepository);
        addFavoriteHandler = new AddFavoriteHandler(ventureRepository, favoriteRepository);
        removeFavoriteHandler = new RemoveFavoriteHandler(favoriteRepository);
        reportHandler = new ReportVentureHandler(ventureRepository, reportRepository, settings);
        resolveReportHandler = new ResolveVentureReportHandler(reportRepository, ventureRepository);
    }

    private CreateVentureCommand validCreateCommand(String name) {
        return new CreateVentureCommand(
                name,
                "Descripción detallada del emprendimiento",
                VentureCategory.COMIDA_Y_BEBIDAS,
                ContactType.WHATSAPP,
                "3001234567",
                Set.of(ServiceMode.PRESENCIAL, ServiceMode.DOMICILIO),
                "Campus Central",
                OWNER
        );
    }

    @Test
    @DisplayName("Registro exitoso: entra en UNDER_REVIEW y activo")
    void createSuccess() {
        VentureDetailDto dto = createHandler.execute(validCreateCommand("Café UFPS"));
        assertNotNull(dto.id());
        assertEquals("Café UFPS", dto.name());
        assertEquals(Status.UNDER_REVIEW, dto.status());
        assertTrue(dto.active());
        assertEquals("+573001234567", dto.contact().value());
        assertEquals(0.0, dto.ratingAverage());
        assertEquals(0, dto.ratingCount());
    }

    @Test
    @DisplayName("Límite de emprendimientos por usuario: máximo 3")
    void maxVenturesPerUserLimit() {
        createHandler.execute(validCreateCommand("V1"));
        createHandler.execute(validCreateCommand("V2"));
        createHandler.execute(validCreateCommand("V3"));

        assertThrows(DomainException.class, () -> createHandler.execute(validCreateCommand("V4")));
    }

    @Test
    @DisplayName("Duplicado para el mismo dueño: mismo nombre normalizado devuelve ConflictException 409")
    void duplicateNormalizedNameSameOwnerThrowsConflict() {
        createHandler.execute(validCreateCommand("Café & Postres"));

        // Variaciones con mayúsculas y tildes
        assertThrows(ConflictException.class, () -> createHandler.execute(validCreateCommand("CAFE & POSTRES")));
    }

    @Test
    @DisplayName("Edición por el dueño o un admin; denegada a terceros")
    void updatePermissions() {
        VentureDetailDto created = createHandler.execute(validCreateCommand("Pinturas"));

        // Tercero no admin -> ForbiddenException
        assertThrows(ForbiddenException.class, () -> updateHandler.execute(new UpdateVentureCommand(
                created.id(), "Pinturas Editadas", "Desc", VentureCategory.ARTE_Y_MANUALIDADES,
                ContactType.WHATSAPP, "3109876543", Set.of(ServiceMode.VIRTUAL), null, OTHER_USER, false
        )));

        // Dueño -> permitido
        VentureDetailDto updated = updateHandler.execute(new UpdateVentureCommand(
                created.id(), "Pinturas Al Óleo", "Desc", VentureCategory.ARTE_Y_MANUALIDADES,
                ContactType.WHATSAPP, "3109876543", Set.of(ServiceMode.VIRTUAL), null, OWNER, false
        ));
        assertEquals("Pinturas Al Óleo", updated.name());

        // Admin -> permitido
        VentureDetailDto adminUpdated = updateHandler.execute(new UpdateVentureCommand(
                created.id(), "Pinturas UFPS", "Desc", VentureCategory.ARTE_Y_MANUALIDADES,
                ContactType.WHATSAPP, "3109876543", Set.of(ServiceMode.VIRTUAL), null, OTHER_USER, true
        ));
        assertEquals("Pinturas UFPS", adminUpdated.name());
    }

    @Test
    @DisplayName("Regla clave: cambiar contacto de uno publicado lo devuelve a UNDER_REVIEW")
    void changingContactOnPublishedReturnsToUnderReview() {
        VentureDetailDto created = createHandler.execute(validCreateCommand("Postres"));
        publishHandler.execute(new PublishVentureCommand(created.id()));

        Venture published = ventureRepository.findById(VentureId.of(created.id())).orElseThrow();
        assertEquals(Status.PUBLISHED, published.getStatus());

        // Cambiar solo el contacto
        updateHandler.execute(new UpdateVentureCommand(
                created.id(), "Postres", "Desc", VentureCategory.COMIDA_Y_BEBIDAS,
                ContactType.INSTAGRAM, "postres_ufps", Set.of(ServiceMode.DOMICILIO), null, OWNER, false
        ));

        Venture reviewed = ventureRepository.findById(VentureId.of(created.id())).orElseThrow();
        assertEquals(Status.UNDER_REVIEW, reviewed.getStatus());
    }

    @Test
    @DisplayName("Regla clave: cambiar otros campos en publicado mantiene estado PUBLISHED")
    void changingNonContactFieldsOnPublishedKeepsPublishedStatus() {
        VentureDetailDto created = createHandler.execute(validCreateCommand("Postres Caseros"));
        publishHandler.execute(new PublishVentureCommand(created.id()));

        // Mismo contacto, diferente descripción y ubicación
        updateHandler.execute(new UpdateVentureCommand(
                created.id(), "Postres Caseros Gourmet", "Nueva descripción", VentureCategory.COMIDA_Y_BEBIDAS,
                ContactType.WHATSAPP, "3001234567", Set.of(ServiceMode.DOMICILIO), "Bloque B", OWNER, false
        ));

        Venture stillPublished = ventureRepository.findById(VentureId.of(created.id())).orElseThrow();
        assertEquals(Status.PUBLISHED, stillPublished.getStatus());
    }

    @Test
    @DisplayName("Activar y desactivar por dueño o admin")
    void activateAndDeactivate() {
        VentureDetailDto created = createHandler.execute(validCreateCommand("Servicio Tech"));

        deactivateHandler.execute(new DeactivateVentureCommand(created.id(), OWNER, false));
        assertFalse(ventureRepository.findById(VentureId.of(created.id())).orElseThrow().isActive());

        activateHandler.execute(new ActivateVentureCommand(created.id(), OWNER, false));
        assertTrue(ventureRepository.findById(VentureId.of(created.id())).orElseThrow().isActive());
    }

    @Test
    @DisplayName("Calificaciones: dueño no puede calificar el propio, solo visibles, reemplazo y borrado")
    void ratingRules() {
        VentureDetailDto created = createHandler.execute(validCreateCommand("Ropa UFPS"));

        // No es visible aún (UNDER_REVIEW) -> DomainException
        assertThrows(DomainException.class, () -> rateHandler.execute(new RateVentureCommand(created.id(), OTHER_USER, 5)));

        // Publicar para hacerlo visible
        publishHandler.execute(new PublishVentureCommand(created.id()));

        // El dueño no puede calificar el propio -> DomainException
        assertThrows(DomainException.class, () -> rateHandler.execute(new RateVentureCommand(created.id(), OWNER, 5)));

        // Calificación válida
        VentureRatingDto rated = rateHandler.execute(new RateVentureCommand(created.id(), OTHER_USER, 4));
        assertEquals(4, rated.score());

        // Reemplazar calificación existente
        VentureRatingDto updated = rateHandler.execute(new RateVentureCommand(created.id(), OTHER_USER, 5));
        assertEquals(5, updated.score());

        // Promedio y total
        RatingSummary summary = ratingRepository.getRatingSummary(VentureId.of(created.id()));
        assertEquals(5.0, summary.average());
        assertEquals(1, summary.total());

        // Borrar calificación
        deleteRatingHandler.execute(new DeleteRatingCommand(created.id(), OTHER_USER));
        RatingSummary emptySummary = ratingRepository.getRatingSummary(VentureId.of(created.id()));
        assertEquals(0.0, emptySummary.average());
        assertEquals(0, emptySummary.total());
    }

    @Test
    @DisplayName("Favoritos: requiere publicado, es idempotente y se puede remover")
    void favoriteRules() {
        VentureDetailDto created = createHandler.execute(validCreateCommand("Tutorías Cálculo"));

        // No publicado -> rechaza
        assertThrows(DomainException.class, () -> addFavoriteHandler.execute(new AddFavoriteCommand(created.id(), OTHER_USER)));

        publishHandler.execute(new PublishVentureCommand(created.id()));

        // Agregar favorito
        addFavoriteHandler.execute(new AddFavoriteCommand(created.id(), OTHER_USER));
        assertTrue(favoriteRepository.existsByAccountIdAndVentureId(AccountId.of(OTHER_USER), VentureId.of(created.id())));

        // Idempotencia: no duplica ni lanza error
        assertDoesNotThrow(() -> addFavoriteHandler.execute(new AddFavoriteCommand(created.id(), OTHER_USER)));

        // Quitar favorito
        removeFavoriteHandler.execute(new RemoveFavoriteCommand(created.id(), OTHER_USER));
        assertFalse(favoriteRepository.existsByAccountIdAndVentureId(AccountId.of(OTHER_USER), VentureId.of(created.id())));
    }

    @Test
    @DisplayName("Reportes: no reporta el propio, único por usuario y umbral de 3 reportes oculta a revisión")
    void reportingRulesAndThreshold() {
        VentureDetailDto created = createHandler.execute(validCreateCommand("Emprendimiento Dudoso"));
        publishHandler.execute(new PublishVentureCommand(created.id()));

        // Dueño no puede reportar el suyo
        assertThrows(DomainException.class, () -> reportHandler.execute(new ReportVentureCommand(
                created.id(), OWNER, ReportReason.FRAUDE_O_ESTAFA, "Mi propio reporte"
        )));

        // Primer reporte
        reportHandler.execute(new ReportVentureCommand(created.id(), "user-1", ReportReason.FRAUDE_O_ESTAFA, "Cobró y no entregó"));

        // Segundo reporte por el mismo usuario -> ConflictException
        assertThrows(ConflictException.class, () -> reportHandler.execute(new ReportVentureCommand(
                created.id(), "user-1", ReportReason.FRAUDE_O_ESTAFA, "Intento duplicado"
        )));

        // Estado sigue PUBLISHED con 1 reporte
        assertEquals(Status.PUBLISHED, ventureRepository.findById(VentureId.of(created.id())).orElseThrow().getStatus());

        // Segundo usuario reporta
        reportHandler.execute(new ReportVentureCommand(created.id(), "user-2", ReportReason.INFORMACION_FALSA, null));
        assertEquals(Status.PUBLISHED, ventureRepository.findById(VentureId.of(created.id())).orElseThrow().getStatus());

        // Tercer usuario reporta -> alcanza el umbral de 3 -> Vuelve automáticamente a UNDER_REVIEW
        reportHandler.execute(new ReportVentureCommand(created.id(), "user-3", ReportReason.CONTACTO_INVALIDO, null));
        assertEquals(Status.UNDER_REVIEW, ventureRepository.findById(VentureId.of(created.id())).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Resolución de reporte por admin: descartar o archivar")
    void resolveReportOptions() {
        VentureDetailDto created = createHandler.execute(validCreateCommand("Emprendimiento Para Moderar"));
        publishHandler.execute(new PublishVentureCommand(created.id()));

        VentureReportDto report1 = reportHandler.execute(new ReportVentureCommand(
                created.id(), "user-1", ReportReason.OTRO, "Duda menor"
        ));

        // Descartar reporte: se marca resuelto, pero el emprendimiento no se archiva
        resolveReportHandler.execute(new ResolveReportCommand(report1.reportId(), "admin-1", ResolveAction.DISCARD));
        assertEquals(ReportStatus.RESOLVED, reportRepository.findById(report1.reportId()).orElseThrow().getStatus());
        assertEquals(Status.PUBLISHED, ventureRepository.findById(VentureId.of(created.id())).orElseThrow().getStatus());

        // Segundo reporte
        VentureReportDto report2 = reportHandler.execute(new ReportVentureCommand(
                created.id(), "user-2", ReportReason.FRAUDE_O_ESTAFA, "Fraude comprobado"
        ));

        // Archivar: se marca resuelto y el emprendimiento pasa a ARCHIVED
        resolveReportHandler.execute(new ResolveReportCommand(report2.reportId(), "admin-1", ResolveAction.ARCHIVE));
        assertEquals(ReportStatus.RESOLVED, reportRepository.findById(report2.reportId()).orElseThrow().getStatus());
        assertEquals(Status.ARCHIVED, ventureRepository.findById(VentureId.of(created.id())).orElseThrow().getStatus());
    }
}

