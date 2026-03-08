package io.github.thirumalx.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.thirumalx.dao.anchor.NotificationAnchorDao;
import io.github.thirumalx.dao.attribute.NotificationRemainderCountAttributeDao;
import io.github.thirumalx.dao.attribute.NotificationSentAtAttributeDao;
import io.github.thirumalx.dao.tie.CertificateNotificationTieDao;
import io.github.thirumalx.dao.tie.NotificationClientTieDao;
import io.github.thirumalx.dto.Notification;
import io.github.thirumalx.model.Attribute;

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
    private final JdbcClient jdbc;

    public NotificationService(NotificationAnchorDao notificationAnchorDao,
            NotificationSentAtAttributeDao sentAtAttributeDao,
            NotificationRemainderCountAttributeDao remainderCountAttributeDao,
            CertificateNotificationTieDao certificateNotificationTieDao,
            NotificationClientTieDao notificationClientTieDao,
            JdbcClient jdbc) {
        this.notificationAnchorDao = notificationAnchorDao;
        this.sentAtAttributeDao = sentAtAttributeDao;
        this.remainderCountAttributeDao = remainderCountAttributeDao;
        this.certificateNotificationTieDao = certificateNotificationTieDao;
        this.notificationClientTieDao = notificationClientTieDao;
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

        return notificationId;
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
