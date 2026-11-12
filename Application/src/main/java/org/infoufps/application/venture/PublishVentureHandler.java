package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.venture.IVentureRepository;
import org.infoufps.domain.venture.Venture;
import org.infoufps.domain.venture.VentureId;

/**
 * Caso de uso: Publicar un emprendimiento (Administrador).
 */
@ApplicationScoped
public class PublishVentureHandler {

    private final IVentureRepository ventureRepository;

    @Inject
    public PublishVentureHandler(IVentureRepository ventureRepository) {
        this.ventureRepository = ventureRepository;
    }

    public void execute(PublishVentureCommand command) {
        Venture venture = ventureRepository.findById(VentureId.of(command.id()))
                .orElseThrow(() -> new NotFoundException("Emprendimiento no encontrado: " + command.id()));

        venture.publish();
        ventureRepository.save(venture);
    }
}

