package io.github.thirumalx.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.thirumalx.dao.anchor.NotificationAnchorDao;
import io.github.thirumalx.dao.attribute.NotificationRemainderCountAttributeDao;
import io.github.thirumalx.dao.attribute.NotificationSentAtAttributeDao;
import io.github.thirumalx.dao.tie.CertificateNotificationTieDao;
import io.github.thirumalx.dao.tie.NotificationClientTieDao;
import io.github.thirumalx.dao.view.CertificateViewDao;
import io.github.thirumalx.dao.view.ClientViewDao;
import io.github.thirumalx.dao.view.UserViewDao;
import io.github.thirumalx.dto.Certificate;
import io.github.thirumalx.dto.Client;
import io.github.thirumalx.dto.Notification;
import io.github.thirumalx.dto.User;
import io.github.thirumalx.model.Attribute;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thirumal M
 */
@Service
public class NotificationService {

        private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

        private final NotificationAnchorDao notificationAnchorDao;
        private final NotificationSentAtAttributeDao sentAtAttributeDao;
        private final NotificationRemainderCountAttributeDao remainderCountAttributeDao;
        private final CertificateNotificationTieDao certificateNotificationTieDao;
        private final NotificationClientTieDao notificationClientTieDao;
        private final MailService mailService;
        private final CertificateViewDao certificateViewDao;
        private final ClientViewDao clientViewDao;
        private final UserViewDao userViewDao;
        private final JdbcClient jdbc;

        @Value("${mail.cc}")
        private String mailCc;

        public NotificationService(NotificationAnchorDao notificationAnchorDao,
                        NotificationSentAtAttributeDao sentAtAttributeDao,
                        NotificationRemainderCountAttributeDao remainderCountAttributeDao,
                        CertificateNotificationTieDao certificateNotificationTieDao,
                        NotificationClientTieDao notificationClientTieDao,
                        MailService mailService,
                        CertificateViewDao certificateViewDao,
                        ClientViewDao clientViewDao,
                        UserViewDao userViewDao,
                        JdbcClient jdbc) {
                this.notificationAnchorDao = notificationAnchorDao;
                this.sentAtAttributeDao = sentAtAttributeDao;
                this.remainderCountAttributeDao = remainderCountAttributeDao;
                this.certificateNotificationTieDao = certificateNotificationTieDao;
                this.notificationClientTieDao = notificationClientTieDao;
                this.mailService = mailService;
                this.certificateViewDao = certificateViewDao;
                this.clientViewDao = clientViewDao;
                this.userViewDao = userViewDao;
                this.jdbc = jdbc;
        }

        @Transactional
        public Long createNotification(Long certificateId, Long clientId, int remainderCount) {
                logger.info("Creating notification for certificate {} and client {}", certificateId, clientId);

                Long notificationId = notificationAnchorDao.insert(Attribute.METADATA_ACTIVE);

                sentAtAttributeDao.insert(notificationId, LocalDateTime.now().toInstant(ZoneOffset.UTC),
                                Attribute.METADATA_ACTIVE);
                remainderCountAttributeDao.insert(notificationId, remainderCount, Attribute.METADATA_ACTIVE);

                certificateNotificationTieDao.insert(certificateId, notificationId, Attribute.METADATA_ACTIVE);
                notificationClientTieDao.insert(notificationId, clientId, Attribute.METADATA_ACTIVE);

                // Fetch details for email
                Certificate certificate = certificateViewDao.findNowById(certificateId).orElse(null);
                Client client = clientViewDao.findNowById(clientId).orElse(null);

                if (certificate != null && client != null && client.getEmail() != null) {
                        Map<String, Object> model = new HashMap<>();
                        model.put("clientName", client.getName());
                        model.put("serialNumber", certificate.getSerialNumber());
                        model.put("expiryDate", certificate.getNotAfter().toString());
                        model.put("path", certificate.getFileName());

                        try {
                                mailService.sendEmail(client.getEmail(),
                                                "Certificate Expiry Reminder - " + certificate.getSerialNumber(),
                                                "expiry-notification.ftl", model);
                        } catch (Exception e) {
                                logger.error("Failed to send email during notification creation: {}", e.getMessage());
                        }
                }

                if (client != null) {
                        List<User> assignedUsers = userViewDao.listAssignedToClient(client.getId());
                        Set<String> notifiedEmails = new LinkedHashSet<>();
                        for (User assignedUser : assignedUsers) {
                                if (assignedUser.getEmail() == null) {
                                        continue;
                                }
                                if (client.getEmail() != null
                                                && assignedUser.getEmail().equalsIgnoreCase(client.getEmail())) {
                                        continue;
                                }
                                if (!notifiedEmails.add(assignedUser.getEmail().toLowerCase())) {
                                        continue;
                                }
                                Map<String, Object> model = new HashMap<>();
                                model.put("recipientName", assignedUser.getName());
                                model.put("clientName", client.getName());
                                model.put("serialNumber", certificate != null ? certificate.getSerialNumber() : "");
                                model.put("expiryDate", certificate != null ? certificate.getNotAfter().toString() : "");
                                model.put("path", certificate != null ? certificate.getFileName() : "");

                                try {
                                        mailService.sendEmail(assignedUser.getEmail(),
                                                        "Certificate Expiry Reminder - " +
                                                                        (certificate != null
                                                                                        ? certificate.getSerialNumber()
                                                                                        : ""),
                                                        "expiry-notification-assigned.ftl",
                                                        model);
                                } catch (Exception e) {
                                        logger.error("Failed to send assigned user notification: {}", e.getMessage());
                                }
                        }
                }

                return notificationId;
        }

        @Transactional
        public void sendRevocationNotification(Long certificateId) {
                logger.info("Sending revocation notification for certificate {}", certificateId);
                Certificate certificate = certificateViewDao.findNowById(certificateId).orElse(null);
                if (certificate == null) {
                        return;
                }

                List<Client> clients = clientViewDao.listByCertificate(certificateId);
                Set<String> toEmails = new LinkedHashSet<>();
                for (Client client : clients) {
                        if (client.getEmail() != null && !client.getEmail().isBlank()) {
                                toEmails.add(client.getEmail().toLowerCase());
                        }
                        List<User> assignedUsers = userViewDao.listAssignedToClient(client.getId());
                        for (User user : assignedUsers) {
                                if (user.getEmail() != null && !user.getEmail().isBlank()) {
                                        toEmails.add(user.getEmail().toLowerCase());
                                }
                        }
                }

                Map<String, Object> model = new HashMap<>();
                model.put("serialNumber", certificate.getSerialNumber());
                model.put("revokedOn",
                                certificate.getRevokedOn() != null ? certificate.getRevokedOn().toString() : "N/A");
                model.put("path", certificate.getPath());

                String subject = "Certificate Revocation Alert - " + certificate.getSerialNumber();
                String templateName = "revocation-notification.ftl";

                if (toEmails.isEmpty()) {
                        logger.warn("No client or assigned user email found for certificate {}. Moving CC to TO.",
                                        certificateId);
                        if (mailCc != null && !mailCc.isBlank()) {
                                mailService.sendEmail(mailCc, null, subject, templateName, model, null, null);
                        }
                } else {
                        String to = String.join(",", toEmails);
                        mailService.sendEmail(to, mailCc, subject, templateName, model, null, null);
                }
        }

    public List<Notification> getNotificationsByCertificate(Long certificateId) {
                return jdbc
                                .sql("""
                                                SELECT nt.nt_id, snt.nt_snt_notification_sentat, rec.nt_rec_notification_remaindercount, ties.ce_id_isnotifiedby, tcl.cl_id_receives
                                                FROM certx.nt_notification nt
                                                JOIN certx.ce_isnotifiedby_nt_notifies ties ON ties.nt_id_notifies = nt.nt_id
                                                JOIN certx.nt_sentto_cl_receives tcl ON tcl.nt_id_sentto = nt.nt_id
                                                LEFT JOIN certx.nt_snt_notification_sentat snt ON snt.nt_snt_nt_id = nt.nt_id
                                                LEFT JOIN certx.nt_rec_notification_remaindercount rec ON rec.nt_rec_nt_id = nt.nt_id
                                                WHERE ties.ce_id_isnotifiedby = :certificateId
                                                ORDER BY snt.nt_snt_notification_sentat DESC
                                                """)
                                .param("certificateId", certificateId)
                                .query((rs, rowNum) -> Notification.builder()
                                                .id(rs.getLong("nt_id"))
                                                .sentAt(rs.getTimestamp("nt_snt_notification_sentat").toLocalDateTime())
                                                .remainderCount(rs.getInt("nt_rec_notification_remaindercount"))
                                                .certificateId(rs.getLong("ce_id_isnotifiedby"))
                                                .clientId(rs.getLong("cl_id_receives"))
                                                .build())
                                .list();
        }
}
