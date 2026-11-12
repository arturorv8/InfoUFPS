package org.infoufps.application.venture;

import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.venture.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryVentureReportRepository implements IVentureReportRepository {

    private final Map<String, VentureReport> storage = new ConcurrentHashMap<>();

    @Override
    public VentureReport save(VentureReport report) {
        storage.put(report.getReportId(), report);
        return report;
    }

    @Override
    public Optional<VentureReport> findById(String reportId) {
        if (reportId == null) return Optional.empty();
        return Optional.ofNullable(storage.get(reportId));
    }

    @Override
    public Optional<VentureReport> findByReporterIdAndVentureId(AccountId reporterId, VentureId ventureId) {
        if (reporterId == null || ventureId == null) return Optional.empty();
        return storage.values().stream()
                .filter(r -> r.getReporterId().equals(reporterId) && r.getVentureId().equals(ventureId))
                .findFirst();
    }

    @Override
    public long countPendingByVentureId(VentureId ventureId) {
        if (ventureId == null) return 0L;
        return storage.values().stream()
                .filter(r -> r.getVentureId().equals(ventureId) && r.getStatus() == ReportStatus.PENDING)
                .count();
    }

    @Override
    public PageResult<VentureReport> findByStatus(ReportStatus status, PageRequest pageRequest) {
        List<VentureReport> list = storage.values().stream()
                .filter(r -> status == null || r.getStatus() == status)
                .sorted(Comparator.comparing(VentureReport::getCreatedAt).reversed())
                .toList();

        int fromIndex = (int) Math.min(pageRequest.offset(), list.size());
        int toIndex = Math.min(fromIndex + pageRequest.size(), list.size());
        List<VentureReport> page = list.subList(fromIndex, toIndex);

        return PageResult.of(page, list.size(), pageRequest);
    }
}

