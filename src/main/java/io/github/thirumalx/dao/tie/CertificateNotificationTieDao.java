package io.github.thirumalx.dao.tie;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.TieDao;
import io.github.thirumalx.dao.columns.TieColumns;
import io.github.thirumalx.model.tie.CertificateNotification;

/**
 * @author Thirumal M
 *         DAO for the static tie CE_isNotifiedBy_NT_notifies linking a
 *         Certificate to
 *         a Notification.
 */
@Repository
public class CertificateNotificationTieDao extends TieDao<CertificateNotification> {

    public CertificateNotificationTieDao(JdbcClient jdbc) {
        super(jdbc, TieColumns.CertificateNotifies.TABLE,
                TieColumns.CertificateNotifies.ANCHOR1,
                TieColumns.CertificateNotifies.ANCHOR2,
                TieColumns.CertificateNotifies.METADATA);
    }

    @Override
    protected RowMapper<CertificateNotification> rowMapper() {
        return (rs, rowNum) -> {
            Long certificateId = rs.getObject(TieColumns.CertificateNotifies.ANCHOR1) != null
                    ? rs.getLong(TieColumns.CertificateNotifies.ANCHOR1)
                    : null;
            Long notificationId = rs.getObject(TieColumns.CertificateNotifies.ANCHOR2) != null
                    ? rs.getLong(TieColumns.CertificateNotifies.ANCHOR2)
                    : null;
            Long metadataId = rs.getObject(TieColumns.CertificateNotifies.METADATA) != null
                    ? rs.getLong(TieColumns.CertificateNotifies.METADATA)
                    : null;
            return new CertificateNotification(certificateId, notificationId, metadataId);
        };
    }

}
