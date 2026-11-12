package org.infoufps.domain.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;

import java.util.Optional;

/**
 * Puerto de Dominio para la gestión y moderación de reportes de emprendimientos.
 */
public interface IVentureReportRepository {

    /**
     * Guarda o actualiza un reporte.
     */
    VentureReport save(VentureReport report);

    /**
     * Busca un reporte por su identificador.
     */
    Optional<VentureReport> findById(String reportId);

    /**
     * Busca si un usuario ya ha reportado un emprendimiento dado.
     */
    Optional<VentureReport> findByReporterIdAndVentureId(AccountId reporterId, VentureId ventureId);

    /**
     * Cuenta la cantidad de reportes pendientes para un emprendimiento específico.
     */
    long countPendingByVentureId(VentureId ventureId);

    /**
     * Lista los reportes según su estado (ej: PENDING o RESOLVED) de forma paginada.
     */
    PageResult<VentureReport> findByStatus(ReportStatus status, PageRequest pageRequest);
}

