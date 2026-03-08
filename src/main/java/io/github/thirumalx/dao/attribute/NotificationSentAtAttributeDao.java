package io.github.thirumalx.dao.attribute;

import java.time.ZoneOffset;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.dao.columns.AttributeColumns;
import io.github.thirumalx.model.attribute.NotificationSentAtAttribute;

/**
 * @author Thirumal M
 *         Static attribute DAO for NT_SNT_Notification_SentAt.
 */
@Repository
public class NotificationSentAtAttributeDao extends AttributeDao<NotificationSentAtAttribute> {

    private final JdbcClient jdbc;

    protected NotificationSentAtAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.NotificationSentAt.TABLE, AttributeColumns.NotificationSentAt.FK,
                AttributeColumns.NotificationSentAt.VALUE, AttributeColumns.NotificationSentAt.METADATA);
        this.jdbc = jdbc;
    }

    /**
     * Insert for timestamp-typed static attribute.
     */
    public java.util.Map<String, Object> insert(Long anchorId, java.time.Instant sentAt, Long metadata) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO " + AttributeColumns.NotificationSentAt.TABLE
                + " (" + AttributeColumns.NotificationSentAt.FK
                + ", " + AttributeColumns.NotificationSentAt.VALUE
                + ", " + AttributeColumns.NotificationSentAt.METADATA
                + ") VALUES (:id, :value, :metadata) RETURNING " + AttributeColumns.NotificationSentAt.FK)
                .param("id", anchorId)
                .param("value", java.sql.Timestamp.from(sentAt))
                .param("metadata", metadata)
                .update(keyHolder);
        return keyHolder.getKeys();
    }

    @Override
    protected RowMapper<NotificationSentAtAttribute> rowMapper() {
        return (rs, rowNum) -> NotificationSentAtAttribute.builder()
                .id(rs.getLong(AttributeColumns.NotificationSentAt.FK))
                .sentAt(rs.getTimestamp(AttributeColumns.NotificationSentAt.VALUE).toInstant()
                        .atOffset(ZoneOffset.UTC))
                .metadata(rs.getLong(AttributeColumns.NotificationSentAt.METADATA))
                .build();
    }

}
