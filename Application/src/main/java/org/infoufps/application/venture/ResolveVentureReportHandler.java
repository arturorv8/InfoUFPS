package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.calendar.Status;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.venture.*;

/**
 * Caso de uso: Resolver un reporte de moderación por parte de un administrador.
 */
@ApplicationScoped
public class ResolveVentureReportHandler {

    private final IVentureReportRepository reportRepository;
    private final IVentureRepository ventureRepository;

    @Inject
    public ResolveVentureReportHandler(IVentureReportRepository reportRepository, IVentureRepository ventureRepository) {
        this.reportRepository = reportRepository;
        this.ventureRepository = ventureRepository;
    }

    public void execute(ResolveReportCommand command) {
        VentureReport report = reportRepository.findById(command.reportId())
                .orElseThrow(() -> new NotFoundException("Reporte no encontrado: " + command.reportId()));

        report.resolve(AccountId.of(command.adminId()));
        reportRepository.save(report);

        if (command.action() == ResolveAction.ARCHIVE) {
            Venture venture = ventureRepository.findById(report.getVentureId())
                    .orElseThrow(() -> new NotFoundException("Emprendimiento reportado no encontrado: " + report.getVentureId()));

            if (venture.getStatus() != Status.ARCHIVED) {
                venture.archive();
                ventureRepository.save(venture);
            }
        }
    }
}

