package io.github.thirumalx.dao.attribute;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.dao.columns.AttributeColumns;
import io.github.thirumalx.model.attribute.UserIdAttribute;

/**
 * @author Thirumal M
 */
@Repository
public class UserIdAttributeDao extends AttributeDao<UserIdAttribute> {

    protected UserIdAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.UserId.TABLE, AttributeColumns.UserId.FK, AttributeColumns.UserId.VALUE,
                AttributeColumns.UserId.CHANGED_AT, AttributeColumns.UserId.METADATA);
    }

    @Override
    protected RowMapper<UserIdAttribute> rowMapper() {
        return (rs, rowNum) -> UserIdAttribute.builder()
                .id(rs.getLong(AttributeColumns.UserId.FK))
                .userId(rs.getString(AttributeColumns.UserId.VALUE))
                .changedAt(rs.getTimestamp(AttributeColumns.UserId.CHANGED_AT).toInstant())
                .metadata(rs.getLong(AttributeColumns.UserId.METADATA))
                .build();
    }
}
