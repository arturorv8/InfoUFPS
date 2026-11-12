package org.infoufps.domain.forum;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PagedResult;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de Dominio para la persistencia y gestión de reportes de moderación del foro.
 */
public interface IForumReportRepository {

    /**
     * Guarda o actualiza un reporte en el almacenamiento.
     */
    ForumReport save(ForumReport report);

    /**
     * Busca un reporte por su identificador único.
     */
    Optional<ForumReport> findById(ReportId id);

    /**
     * Verifica si un usuario ya reportó un recurso destino específico.
     */
    boolean existsByReporterAndTarget(AccountId reporterId, ReportTargetType targetType, String targetId);

    /**
     * Cuenta el total de reportes pendientes de moderación asociados a un recurso destino.
     */
    long countPendingReportsByTarget(ReportTargetType targetType, String targetId);

    /**
     * Obtiene la lista de reportes pendientes de moderación asociados a un recurso destino.
     */
    List<ForumReport> findPendingReportsByTarget(ReportTargetType targetType, String targetId);

    /**
     * Consulta paginada para administradores, con filtro opcional por estado de resolución.
     */
    PagedResult<ForumReport> findAdmin(ReportStatus status, PageRequest pageRequest);
}

