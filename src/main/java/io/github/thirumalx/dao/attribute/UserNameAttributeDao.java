package io.github.thirumalx.dao.attribute;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.dao.columns.AttributeColumns;
import io.github.thirumalx.model.attribute.UserNameAttribute;

/**
 * @author Thirumal M
 */
@Repository
public class UserNameAttributeDao extends AttributeDao<UserNameAttribute> {

    protected UserNameAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.UserName.TABLE, AttributeColumns.UserName.FK, AttributeColumns.UserName.VALUE,
                AttributeColumns.UserName.CHANGED_AT, AttributeColumns.UserName.METADATA);
    }

    @Override
    protected RowMapper<UserNameAttribute> rowMapper() {
        return (rs, rowNum) -> UserNameAttribute.builder()
                .id(rs.getLong(AttributeColumns.UserName.FK))
                .name(rs.getString(AttributeColumns.UserName.VALUE))
                .changedAt(rs.getTimestamp(AttributeColumns.UserName.CHANGED_AT).toInstant())
                .metadata(rs.getLong(AttributeColumns.UserName.METADATA))
                .build();
    }
}
