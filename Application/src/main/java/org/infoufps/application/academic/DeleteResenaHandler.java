package org.infoufps.application.academic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.infoufps.domain.academic.Resena;
import org.infoufps.domain.academic.EstadoModeracion;
import org.infoufps.domain.shared.exceptions.ForbiddenException;
import org.infoufps.domain.shared.exceptions.ResourceNotFoundException;
import org.infoufps.domain.academic.IResenaRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.academic.ResenaId;

@ApplicationScoped
public class DeleteResenaHandler {

    private final IResenaRepository resenaRepository;

    @Inject
    public DeleteResenaHandler(IResenaRepository resenaRepository) {
        this.resenaRepository = resenaRepository;
    }

    @Transactional
    public void execute(String resenaIdStr, String requesterIdStr, boolean isAdmin) {
        ResenaId resenaId = ResenaId.of(resenaIdStr);
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la reseña con ID: " + resenaIdStr));

        AccountId requesterId = AccountId.of(requesterIdStr);
        if (!isAdmin && !resena.getAutorId().equals(requesterId)) {
            throw new ForbiddenException("No tienes permiso para eliminar esta reseña");
        }

        // Operación idempotente: si ya está archivada no hay nada que hacer.
        if (resena.getEstado() == EstadoModeracion.ARCHIVADO) {
            return;
        }

        resena.archive();
        resenaRepository.save(resena);
    }
}

