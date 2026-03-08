package io.github.thirumalx.dao.anchor;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AnchorDao;
import io.github.thirumalx.dao.columns.AnchorColumns;
import io.github.thirumalx.model.anchor.NotificationAnchor;

/**
 * @author Thirumal M
 */
@Repository
public class NotificationAnchorDao extends AnchorDao<NotificationAnchor> {

    protected NotificationAnchorDao(JdbcClient jdbc) {
        super(jdbc, AnchorColumns.Notification.TABLE, AnchorColumns.Notification.ID,
                AnchorColumns.Notification.METADATA);
    }

    @Override
    protected RowMapper<NotificationAnchor> rowMapper() {
        return (rs, rowNum) -> new NotificationAnchor(rs.getLong(AnchorColumns.Notification.ID),
                rs.getLong(AnchorColumns.Notification.METADATA));
    }

}
