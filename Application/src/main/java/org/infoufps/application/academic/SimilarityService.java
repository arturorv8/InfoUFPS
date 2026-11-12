package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.application.academic.ModeracionWarningDto;
import org.infoufps.domain.academic.Materia;
import org.infoufps.domain.academic.Profesor;
import org.infoufps.domain.academic.Facultad;
import org.infoufps.domain.academic.IMateriaRepository;
import org.infoufps.domain.academic.IProfesorRepository;
import org.infoufps.domain.shared.text.TextNormalizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de detección de duplicados o entidades similares para moderación.
 * <p>
 * NOTA DE ARQUITECTURA / ESCALABILIDAD:
 * La comparación de similitud por distancia Levenshtein se realiza en memoria sobre candidatos
 * activos prefiltrados en SQL (limitados a 100). Para catálogos a escala masiva (>10.000 entidades),
 * se recomienda migrar a un índice trigram (pg_trgm) nativo en PostgreSQL.
 */
@ApplicationScoped
public class SimilarityService {

    private final IProfesorRepository profesorRepository;
    private final IMateriaRepository materiaRepository;

    @Inject
    public SimilarityService(IProfesorRepository profesorRepository, IMateriaRepository materiaRepository) {
        this.profesorRepository = profesorRepository;
        this.materiaRepository = materiaRepository;
    }

    public List<ModeracionWarningDto> checkProfesorSimilarity(String nombre, Facultad facultad) {
        List<ModeracionWarningDto> warnings = new ArrayList<>();
        if (profesorRepository == null || nombre == null || nombre.isBlank()) {
            return warnings;
        }

        String normalized = TextNormalizer.normalizeAndSortWords(nombre);
        List<Profesor> candidates = profesorRepository.findSimilar(normalized, facultad);

        for (Profesor p : candidates) {
            double sim = TextNormalizer.calculateSimilarity(nombre, p.getNombre());
            if (sim >= 0.70) {
                warnings.add(new ModeracionWarningDto(
                        p.getId().value(),
                        p.getNombre(),
                        p.getEstado().name(),
                        Math.round(sim * 100.0) / 100.0,
                        "Existe un profesor con nombre similar (" + (int)(sim * 100) + "% de similitud): '" + p.getNombre() + "'"
                ));
            }
        }
        return warnings;
    }

    public List<ModeracionWarningDto> checkMateriaSimilarity(String nombre) {
        List<ModeracionWarningDto> warnings = new ArrayList<>();
        if (materiaRepository == null || nombre == null || nombre.isBlank()) {
            return warnings;
        }

        List<Materia> candidates = materiaRepository.findSimilar();

        for (Materia m : candidates) {
            double sim = TextNormalizer.calculateSimilarity(nombre, m.getNombre());
            if (sim >= 0.70) {
                warnings.add(new ModeracionWarningDto(
                        m.getId().value(),
                        m.getNombre(),
                        m.getEstado().name(),
                        Math.round(sim * 100.0) / 100.0,
                        "Existe una materia con nombre similar (" + (int)(sim * 100) + "% de similitud): '" + m.getNombre() + "'"
                ));
            }
        }
        return warnings;
    }
}

