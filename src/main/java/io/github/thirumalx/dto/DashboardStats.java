package io.github.thirumalx.dto;

import java.util.List;

/**
 * @author Thirumal
 */
public record DashboardStats(
        long totalApplications,
        long totalClients,
        long totalCertificates,
        List<StatusCount> statusDistribution,
        List<ExpiryCount> expiryStats,
        List<AppCount> topApplications) {
    public record StatusCount(String status, long count) {
    }

    public record ExpiryCount(String month, long count) {
    }

    public record AppCount(String name, long count) {
    }
}
