package org.infoufps.application.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.venture.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Consultas (Queries) de Venture")
class VentureQueriesTest {

    private InMemoryVentureRepository ventureRepository;
    private InMemoryVentureRatingRepository ratingRepository;
    private InMemoryFavoriteVentureRepository favoriteRepository;
    private InMemoryVentureReportRepository reportRepository;
    private TestVentureSettings settings;

    private SearchVenturesQuery searchQuery;
    private GetVentureDetailQuery detailQuery;
    private GetMyVenturesQuery myVenturesQuery;
    private GetMyFavoritesQuery myFavoritesQuery;
    private GetMyFavoriteIdsQuery myFavoriteIdsQuery;
    private GetAdminVenturesQuery adminVenturesQuery;
    private GetAdminVentureReportsQuery adminReportsQuery;
    private GetVentureShareQuery shareQuery;
    private GetVentureSharePageQuery sharePageQuery;

    private final AccountId owner1 = AccountId.of("owner-1");
    private final AccountId owner2 = AccountId.of("owner-2");

    @BeforeEach
    void setUp() {
        ventureRepository = new InMemoryVentureRepository();
        ratingRepository = new InMemoryVentureRatingRepository();
        favoriteRepository = new InMemoryFavoriteVentureRepository(ventureRepository::findById);
        reportRepository = new InMemoryVentureReportRepository();
        settings = new TestVentureSettings();

        ventureRepository.setRatingProvider(ratingRepository::getRatingSummary);

        searchQuery = new SearchVenturesQuery(ventureRepository, ratingRepository);
        detailQuery = new GetVentureDetailQuery(ventureRepository, ratingRepository);
        myVenturesQuery = new GetMyVenturesQuery(ventureRepository, ratingRepository);
        myFavoritesQuery = new GetMyFavoritesQuery(favoriteRepository, ratingRepository);
        myFavoriteIdsQuery = new GetMyFavoriteIdsQuery(favoriteRepository);
        adminVenturesQuery = new GetAdminVenturesQuery(ventureRepository, ratingRepository);
        adminReportsQuery = new GetAdminVentureReportsQuery(reportRepository);
        shareQuery = new GetVentureShareQuery(ventureRepository, settings);
        sharePageQuery = new GetVentureSharePageQuery(ventureRepository, settings);
    }

    private Venture createAndSave(String name, VentureCategory category, Set<ServiceMode> modes, AccountId owner, boolean publish) {
        Venture v = Venture.create(
                name,
                "Descripción de prueba para " + name,
                category,
                owner,
                Contact.of(ContactType.WHATSAPP, "3001234567"),
                modes,
                "Campus UFPS"
        );
        if (publish) {
            v.publish();
        }
        return ventureRepository.save(v);
    }

    @Test
    @DisplayName("Búsqueda: palabras en cualquier orden, sin tildes ni mayúsculas y solo visibles")
    void searchWordsAnyOrderAndAccents() {
        // v1: visible
        Venture v1 = createAndSave("Café & Pastelería UFPS", VentureCategory.COMIDA_Y_BEBIDAS, Set.of(ServiceMode.PRESENCIAL), owner1, true);
        // v2: no visible (UNDER_REVIEW)
        createAndSave("Café Del Bosque", VentureCategory.COMIDA_Y_BEBIDAS, Set.of(ServiceMode.PRESENCIAL), owner2, false);
        // v3: visible otro rubro
        createAndSave("Libros y Tutorías", VentureCategory.SERVICIOS_Y_TUTORIAS, Set.of(ServiceMode.VIRTUAL), owner2, true);

        // Búsqueda con tildes invertidas y orden alterno: "pasteleria cafe"
        VentureSearchCriteria criteria = VentureSearchCriteria.of(
                "pasteleria cafe", null, null, "name", PageRequest.first()
        );
        PageResult<VentureCardDto> result = searchQuery.execute(criteria);

        assertEquals(1, result.total());
        assertEquals(v1.getId().value(), result.items().get(0).id());
    }

    @Test
    @DisplayName("Búsqueda: filtros múltiples por categoría y modos de servicio")
    void searchCategoryAndModeFilters() {
        Venture v1 = createAndSave("Comida Rápida", VentureCategory.COMIDA_Y_BEBIDAS, Set.of(ServiceMode.DOMICILIO), owner1, true);
        Venture v2 = createAndSave("Almuerzos Caseros", VentureCategory.COMIDA_Y_BEBIDAS, Set.of(ServiceMode.PRESENCIAL), owner2, true);
        Venture v3 = createAndSave("Camisetas UFPS", VentureCategory.MODA_Y_ACCESORIOS, Set.of(ServiceMode.DOMICILIO), owner1, true);

        // Filtro por COMIDA_Y_BEBIDAS y modo DOMICILIO -> Solo debe retornar v1
        VentureSearchCriteria criteria = VentureSearchCriteria.of(
                null, Set.of(VentureCategory.COMIDA_Y_BEBIDAS), Set.of(ServiceMode.DOMICILIO), "name", PageRequest.first()
        );
        PageResult<VentureCardDto> result = searchQuery.execute(criteria);

        assertEquals(1, result.total());
        assertEquals(v1.getId().value(), result.items().get(0).id());
    }

    @Test
    @DisplayName("Búsqueda: orden sort=rating (promedio DESC, total DESC, nombre ASC)")
    void searchSortedByRating() {
        Venture vA = createAndSave("Alpha", VentureCategory.TECNOLOGIA, Set.of(ServiceMode.VIRTUAL), owner1, true);
        Venture vB = createAndSave("Beta", VentureCategory.TECNOLOGIA, Set.of(ServiceMode.VIRTUAL), owner2, true);
        Venture vC = createAndSave("Charlie", VentureCategory.TECNOLOGIA, Set.of(ServiceMode.VIRTUAL), owner1, true);

        // Calificaciones:
        // Beta: 5 estrellas (promedio 5.0, total 1)
        ratingRepository.save(VentureRating.create(AccountId.of("u1"), vB.getId(), 5));
        // Charlie: 4 estrellas x 2 (promedio 4.0, total 2)
        ratingRepository.save(VentureRating.create(AccountId.of("u1"), vC.getId(), 4));
        ratingRepository.save(VentureRating.create(AccountId.of("u2"), vC.getId(), 4));
        // Alpha: sin calificaciones (promedio 0.0)

        VentureSearchCriteria criteria = VentureSearchCriteria.of(null, null, null, "rating", PageRequest.first());
        PageResult<VentureCardDto> result = searchQuery.execute(criteria);

        assertEquals(3, result.total());
        assertEquals(vB.getId().value(), result.items().get(0).id(), "Primer lugar: Beta con 5.0");
        assertEquals(vC.getId().value(), result.items().get(1).id(), "Segundo lugar: Charlie con 4.0");
        assertEquals(vA.getId().value(), result.items().get(2).id(), "Tercer lugar: Alpha sin ratings");
    }

    @Test
    @DisplayName("Detalle público: incluye contacto, URL y rating del usuario actual; 404 si no es visible")
    void getDetailPublicAndMyRating() {
        Venture v = createAndSave("Tienda Virtual", VentureCategory.TECNOLOGIA, Set.of(ServiceMode.VIRTUAL), owner1, false);

        // No visible -> 404
        assertThrows(NotFoundException.class, () -> detailQuery.execute(v.getId().value(), "user-1"));

        v.publish();
        ratingRepository.save(VentureRating.create(AccountId.of("user-1"), v.getId(), 4));

        VentureDetailDto detail = detailQuery.execute(v.getId().value(), "user-1");
        assertNotNull(detail.contact());
        assertEquals("+573001234567", detail.contact().value());
        assertEquals("https://wa.me/573001234567", detail.contact().actionUrl());
        assertEquals(4.0, detail.ratingAverage());
        assertEquals(1, detail.ratingCount());
        assertEquals(4, detail.myRating());
    }

    @Test
    @DisplayName("Favoritos: un emprendimiento archivado se conserva pero NO se lista")
    void favoritesExcludesArchivedVentures() {
        Venture v1 = createAndSave("Favorito Uno", VentureCategory.OTROS, Set.of(ServiceMode.PRESENCIAL), owner1, true);
        Venture v2 = createAndSave("Favorito Dos", VentureCategory.OTROS, Set.of(ServiceMode.PRESENCIAL), owner2, true);

        favoriteRepository.save(FavoriteVenture.create(AccountId.of("fan-1"), v1.getId()));
        favoriteRepository.save(FavoriteVenture.create(AccountId.of("fan-1"), v2.getId()));

        assertEquals(2, myFavoritesQuery.execute("fan-1", PageRequest.first()).total());

        // Archivar v2
        v2.archive();
        ventureRepository.save(v2);

        // La relación sigue existiendo en base de datos
        assertTrue(favoriteRepository.existsByAccountIdAndVentureId(AccountId.of("fan-1"), v2.getId()));

        // Pero la consulta solo devuelve los PUBLISHED
        PageResult<VentureCardDto> activeFavorites = myFavoritesQuery.execute("fan-1", PageRequest.first());
        assertEquals(1, activeFavorites.total());
        assertEquals(v1.getId().value(), activeFavorites.items().get(0).id());
    }

    @Test
    @DisplayName("Admin: marca advertencia cuando otro dueño tiene el mismo nombre o contacto")
    void adminDuplicateWarnings() {
        // Dueño 1 crea emprendimiento
        Venture v1 = Venture.create(
                "Hamburguesas UFPS", "Desc", VentureCategory.COMIDA_Y_BEBIDAS,
                owner1, Contact.of(ContactType.WHATSAPP, "3151112233"), Set.of(ServiceMode.PRESENCIAL), null
        );
        ventureRepository.save(v1);

        // Dueño 2 crea emprendimiento con el mismo nombre y contacto
        Venture v2 = Venture.create(
                "HAMBURGUESAS UFPS", "Desc", VentureCategory.COMIDA_Y_BEBIDAS,
                owner2, Contact.of(ContactType.WHATSAPP, "3151112233"), Set.of(ServiceMode.PRESENCIAL), null
        );
        ventureRepository.save(v2);

        PageResult<AdminVentureDto> adminPage = adminVenturesQuery.execute(null, PageRequest.first());
        AdminVentureDto dtoV2 = adminPage.items().stream()
                .filter(d -> d.id().equals(v2.getId().value()))
                .findFirst()
                .orElseThrow();

        assertTrue(dtoV2.duplicateNameWarning(), "Debe marcar advertencia por coincidencia de nombre con otro dueño");
        assertTrue(dtoV2.duplicateContactWarning(), "Debe marcar advertencia por coincidencia de contacto con otro dueño");
    }

    @Test
    @DisplayName("Compartir: escapa caracteres HTML y 404 si no está visible")
    void shareQueryEscapesHtmlAndRequiresVisible() {
        Venture v = Venture.create(
                "<script>alert('xss')</script> Café & Pan",
                "Descripción con <tags> y \"comillas\"",
                VentureCategory.COMIDA_Y_BEBIDAS,
                owner1,
                Contact.of(ContactType.WHATSAPP, "3001234567"),
                Set.of(ServiceMode.PRESENCIAL),
                null
        );
        ventureRepository.save(v);

        // No visible -> 404
        assertThrows(NotFoundException.class, () -> shareQuery.execute(v.getId().value()));
        assertThrows(NotFoundException.class, () -> sharePageQuery.execute(v.getId().value()));

        v.publish();
        ventureRepository.save(v);

        // Compartir metadatos JSON
        VentureShareDto shareDto = shareQuery.execute(v.getId().value());
        assertNotNull(shareDto.shareUrl());
        assertEquals("http://localhost:5173/logo.png", shareDto.logoUrl());

        // Página HTML de vista previa
        VentureSharePageDto page = sharePageQuery.execute(v.getId().value());
        assertTrue(page.html().contains("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"));
        assertTrue(page.html().contains("&lt;tags&gt;"));
        assertFalse(page.html().contains("<script>alert('xss')</script>"));
    }
}

