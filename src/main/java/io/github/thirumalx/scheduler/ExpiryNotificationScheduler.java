package io.github.thirumalx.scheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.github.thirumalx.service.ExcelReportService;
import io.github.thirumalx.service.ExcelReportService.CertificateReportItem;
import io.github.thirumalx.service.MailService;
import io.github.thirumalx.service.NotificationService;

/**
 * @author Thirumal M
 */
@Component
public class ExpiryNotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ExpiryNotificationScheduler.class);

    public record CertificateReportResponse(String message, List<CertificateReportItem> items) {
    }

    @Value("${report.generate-for-next-n-days}")
    private int generateForNextNDays;

    @Value("${mail.report-to}")
    private String reportTo;

    private final NotificationService notificationService;
    private final ExcelReportService excelReportService;
    private final MailService mailService;
    private final JdbcClient jdbc;

    public ExpiryNotificationScheduler(NotificationService notificationService, ExcelReportService excelReportService,
            MailService mailService, JdbcClient jdbc) {
        this.notificationService = notificationService;
        this.excelReportService = excelReportService;
        this.mailService = mailService;
        this.jdbc = jdbc;
    }

    /**
     * Runs daily at midnight to check for expiring certificates.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void checkExpiringCertificates() {
        logger.info("Running expiry notification check...");

        // Find certificates expiring within 30 days that don't have a notification in
        // the last 7 days
        String sql = """
                SELECT nce.ce_id, tie.cl_id_owns, nce.ce_naf_certificate_notafter
                FROM certx.nce_certificate nce
                JOIN certx.ce_belongsto_cl_owns tie ON tie.ce_id_belongsto = nce.ce_id
                WHERE nce.ce_ron_certificate_revokedon IS NULL
                AND nce.ce_naf_certificate_notafter BETWEEN NOW() AND NOW() + INTERVAL '" + generateForNextNDays + " days'
                AND nce.ce_id NOT IN (
                    SELECT cnt.ce_id_isnotifiedby
                    FROM certx.ce_isnotifiedby_nt_notifies cnt
                    JOIN certx.nt_snt_notification_sentat snt ON snt.nt_snt_nt_id = cnt.nt_id_notifies
                    WHERE snt.nt_snt_notification_sentat > NOW() - INTERVAL '7 days'
                )
                """;

        List<ExpiringCert> expiringCerts = jdbc.sql(sql)
                .query((rs, rowNum) -> new ExpiringCert(
                        rs.getLong("ce_id"),
                        rs.getLong("cl_id_owns")))
                .list();

        for (ExpiringCert cert : expiringCerts) {
            try {
                // Determine remainder count (e.g., 30, 15, 7 etc.)
                // For simplicity, we create one notification every 7 days as long as it's
                // within 30 days of expiry
                notificationService.createNotification(cert.certificateId, cert.clientId, 0);
                logger.debug("Created notification for certificate {}", cert.certificateId);
            } catch (Exception e) {
                logger.error("Failed to create notification for certificate {}: {}", cert.certificateId,
                        e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * *") // Run at 1 AM
    public CertificateReportResponse generateAndSendDailyReport() {
        logger.info("Generating and sending daily reports...");

        List<CertificateReportItem> reportItems = new ArrayList<>();

        // 1. Expired Today
        reportItems.addAll(fetchCerts("Expired Today",
                "nce.ce_naf_certificate_notafter::date = CURRENT_DATE"));

        // 2. Revoked Today
        reportItems.addAll(fetchCerts("Revoked Today",
                "nce.ce_ron_certificate_revokedon::date = CURRENT_DATE"));

        // 3. Expiring in next n days (excluding today)
        reportItems.addAll(fetchCerts("Expiring Soon",
                "nce.ce_naf_certificate_notafter BETWEEN CURRENT_DATE + INTERVAL '1 day' AND CURRENT_DATE + INTERVAL '"
                        + generateForNextNDays + " days'"));

        if (reportItems.isEmpty()) {
            logger.info("No items to include in report. Skipping email.");
            return new CertificateReportResponse("No certificates found to include in the report.", List.of());
        }

        try {
            byte[] report = excelReportService.generateReport(reportItems);
            mailService.sendEmail(reportTo, "Daily Certificate Status Report", "daily-report.ftl",
                    Map.of("date", LocalDateTime.now().toString(), "items", reportItems), "CertificateReport.xlsx",
                    report);
            logger.info("Daily report sent to {}", reportTo);
            return new CertificateReportResponse("Report generated and sent to " + reportTo, reportItems);
        } catch (Exception e) {
            logger.error("Failed to generate or send daily report", e);
            return new CertificateReportResponse("Failed to generate report: " + e.getMessage(), reportItems);
        }
    }

    private List<CertificateReportItem> fetchCerts(String category, String condition) {
        String sql = """
                SELECT nce.ce_id, tie.cl_id_owns, nce.ce_naf_certificate_notafter, nce.ce_ron_certificate_revokedon
                FROM certx.nce_certificate nce
                JOIN certx.ce_belongsto_cl_owns tie ON tie.ce_id_belongsto = nce.ce_id
                WHERE\s""" + condition;
        return jdbc.sql(sql)
                .query((rs, rowNum) -> CertificateReportItem.builder()
                        .category(category)
                        .certificateId(rs.getLong("ce_id"))
                        .clientId(rs.getObject("cl_id_owns") != null ? rs.getLong("cl_id_owns") : null)
                        .expiryDate(rs.getTimestamp("ce_naf_certificate_notafter") != null
                                ? rs.getTimestamp("ce_naf_certificate_notafter").toLocalDateTime()
                                : null)
                        .revokedOn(rs.getTimestamp("ce_ron_certificate_revokedon") != null
                                ? rs.getTimestamp("ce_ron_certificate_revokedon").toLocalDateTime()
                                : null)
                        .build())
                .list();
    }

    private record ExpiringCert(Long certificateId, Long clientId) {
    }

}
