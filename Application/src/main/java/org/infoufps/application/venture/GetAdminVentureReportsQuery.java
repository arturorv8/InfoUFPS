package org.infoufps.application.venture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.venture.IVentureReportRepository;
import org.infoufps.domain.venture.ReportStatus;
import org.infoufps.domain.venture.VentureReport;

import java.util.List;

/**
 * Consulta de la lista de reportes de moderación para administradores.
 */
@ApplicationScoped
public class GetAdminVentureReportsQuery {

    private final IVentureReportRepository reportRepository;

    @Inject
    public GetAdminVentureReportsQuery(IVentureReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public PageResult<VentureReportDto> execute(ReportStatus status, PageRequest pageRequest) {
        PageResult<VentureReport> page = reportRepository.findByStatus(status, pageRequest);

        List<VentureReportDto> dtos = page.items().stream()
                .map(VentureReportDto::from)
                .toList();

        return PageResult.of(dtos, page.total(), pageRequest);
    }
}

