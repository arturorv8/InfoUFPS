package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.exceptions.ConflictException;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.NotFoundException;
import org.infoufps.domain.venture.*;

/**
 * Caso de uso: Reportar un emprendimiento para moderación.
 */
@ApplicationScoped
public class ReportVentureHandler {

    private final IVentureRepository ventureRepository;
    private final IVentureReportRepository reportRepository;
    private final VentureSettings settings;

    @Inject
    public ReportVentureHandler(IVentureRepository ventureRepository,
                                IVentureReportRepository reportRepository,
                                VentureSettings settings) {
        this.ventureRepository = ventureRepository;
        this.reportRepository = reportRepository;
        this.settings = settings;
    }

    public VentureReportDto execute(ReportVentureCommand command) {
        Venture venture = ventureRepository.findById(VentureId.of(command.ventureId()))
                .orElseThrow(() -> new NotFoundException("Emprendimiento no encontrado: " + command.ventureId()));

        AccountId reporterId = AccountId.of(command.reporterId());
        if (venture.getOwnerId().equals(reporterId)) {
            throw new DomainException("No puedes reportar tu propio emprendimiento");
        }

        // Un reporte por usuario
        if (reportRepository.findByReporterIdAndVentureId(reporterId, venture.getId()).isPresent()) {
            throw new ConflictException("Ya has reportado este emprendimiento previamente");
        }

        VentureReport report = VentureReport.create(
                reporterId,
                venture.getId(),
                command.reason(),
                command.comment()
        );
        reportRepository.save(report);

        // Si alcanza el umbral de reportes pendientes (configurable, por defecto 3), vuelve a UNDER_REVIEW
        long pendingCount = reportRepository.countPendingByVentureId(venture.getId());
        if (pendingCount >= settings.reportThresholdForReview()) {
            venture.returnToReview();
            ventureRepository.save(venture);
        }

        return VentureReportDto.from(report);
    }
}

