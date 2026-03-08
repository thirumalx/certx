package io.github.thirumalx.dao.attribute;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.dao.columns.AttributeColumns;
import io.github.thirumalx.model.attribute.NotificationRemainderCountAttribute;

/**
 * @author Thirumal M
 *         Static attribute DAO for NT_REC_Notification_RemainderCount.
 */
@Repository
public class NotificationRemainderCountAttributeDao extends AttributeDao<NotificationRemainderCountAttribute> {

    private final JdbcClient jdbc;

    protected NotificationRemainderCountAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.NotificationRemainderCount.TABLE, AttributeColumns.NotificationRemainderCount.FK,
                AttributeColumns.NotificationRemainderCount.VALUE,
                AttributeColumns.NotificationRemainderCount.METADATA);
        this.jdbc = jdbc;
    }

    /**
     * Insert for integer-typed static attribute.
     */
    public java.util.Map<String, Object> insert(Long anchorId, int remainderCount, Long metadata) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO " + AttributeColumns.NotificationRemainderCount.TABLE
                + " (" + AttributeColumns.NotificationRemainderCount.FK
                + ", " + AttributeColumns.NotificationRemainderCount.VALUE
                + ", " + AttributeColumns.NotificationRemainderCount.METADATA
                + ") VALUES (:id, :value, :metadata) RETURNING " + AttributeColumns.NotificationRemainderCount.FK)
                .param("id", anchorId)
                .param("value", remainderCount)
                .param("metadata", metadata)
                .update(keyHolder);
        return keyHolder.getKeys();
    }

    @Override
    protected RowMapper<NotificationRemainderCountAttribute> rowMapper() {
        return (rs, rowNum) -> NotificationRemainderCountAttribute.builder()
                .id(rs.getLong(AttributeColumns.NotificationRemainderCount.FK))
                .remainderCount(rs.getInt(AttributeColumns.NotificationRemainderCount.VALUE))
                .metadata(rs.getLong(AttributeColumns.NotificationRemainderCount.METADATA))
                .build();
    }

}
