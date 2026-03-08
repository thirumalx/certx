package io.github.thirumalx.dao.tie;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.TieDao;
import io.github.thirumalx.dao.columns.TieColumns;
import io.github.thirumalx.model.tie.NotificationClient;

/**
 * @author Thirumal M
 *         DAO for the static tie NT_sentTo_CL_receives linking a Notification
 *         to
 *         a Client receiver.
 */
@Repository
public class NotificationClientTieDao extends TieDao<NotificationClient> {

    public NotificationClientTieDao(JdbcClient jdbc) {
        super(jdbc, TieColumns.NotificationReceivedBy.TABLE,
                TieColumns.NotificationReceivedBy.ANCHOR1,
                TieColumns.NotificationReceivedBy.ANCHOR2,
                TieColumns.NotificationReceivedBy.METADATA);
    }

    @Override
    protected RowMapper<NotificationClient> rowMapper() {
        return (rs, rowNum) -> {
            Long notificationId = rs.getObject(TieColumns.NotificationReceivedBy.ANCHOR1) != null
                    ? rs.getLong(TieColumns.NotificationReceivedBy.ANCHOR1)
                    : null;
            Long clientId = rs.getObject(TieColumns.NotificationReceivedBy.ANCHOR2) != null
                    ? rs.getLong(TieColumns.NotificationReceivedBy.ANCHOR2)
                    : null;
            Long metadataId = rs.getObject(TieColumns.NotificationReceivedBy.METADATA) != null
                    ? rs.getLong(TieColumns.NotificationReceivedBy.METADATA)
                    : null;
            return new NotificationClient(notificationId, clientId, metadataId);
        };
    }

}
