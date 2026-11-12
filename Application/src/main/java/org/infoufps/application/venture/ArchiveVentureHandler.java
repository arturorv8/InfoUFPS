package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.venture.IVentureRepository;
import org.infoufps.domain.venture.Venture;
import org.infoufps.domain.venture.VentureId;

/**
 * Caso de uso: Archivar un emprendimiento (Administrador).
 */
@ApplicationScoped
public class ArchiveVentureHandler {

    private final IVentureRepository ventureRepository;

    @Inject
    public ArchiveVentureHandler(IVentureRepository ventureRepository) {
        this.ventureRepository = ventureRepository;
    }

    public void execute(ArchiveVentureCommand command) {
        Venture venture = ventureRepository.findById(VentureId.of(command.id()))
                .orElseThrow(() -> new NotFoundException("Emprendimiento no encontrado: " + command.id()));

        venture.archive();
        ventureRepository.save(venture);
    }
}

