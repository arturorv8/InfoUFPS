package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.venture.IVentureRepository;
import org.infoufps.domain.venture.Venture;
import org.infoufps.domain.venture.VentureId;

/**
 * Caso de uso: Activar un emprendimiento.
 */
@ApplicationScoped
public class ActivateVentureHandler {

    private final IVentureRepository ventureRepository;

    @Inject
    public ActivateVentureHandler(IVentureRepository ventureRepository) {
        this.ventureRepository = ventureRepository;
    }

    public void execute(ActivateVentureCommand command) {
        Venture venture = ventureRepository.findById(VentureId.of(command.id()))
                .orElseThrow(() -> new NotFoundException("Emprendimiento no encontrado: " + command.id()));

        venture.activate(AccountId.of(command.requesterId()), command.isAdmin());
        ventureRepository.save(venture);
    }
}

