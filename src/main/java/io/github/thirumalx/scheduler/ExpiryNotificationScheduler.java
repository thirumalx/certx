package io.github.thirumalx.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.github.thirumalx.service.NotificationService;

/**
 * @author Thirumal M
 */
@Component
public class ExpiryNotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ExpiryNotificationScheduler.class);

    private final NotificationService notificationService;
    private final JdbcClient jdbc;

    public ExpiryNotificationScheduler(NotificationService notificationService, JdbcClient jdbc) {
        this.notificationService = notificationService;
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
                AND nce.ce_naf_certificate_notafter BETWEEN NOW() AND NOW() + INTERVAL '30 days'
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

    private record ExpiringCert(Long certificateId, Long clientId) {
    }

}
