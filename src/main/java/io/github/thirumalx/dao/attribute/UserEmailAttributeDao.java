package io.github.thirumalx.dao.attribute;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.dao.columns.AttributeColumns;
import io.github.thirumalx.model.attribute.UserEmailAttribute;

/**
 * @author Thirumal M
 */
@Repository
public class UserEmailAttributeDao extends AttributeDao<UserEmailAttribute> {

    protected UserEmailAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.UserEmail.TABLE, AttributeColumns.UserEmail.FK,
                AttributeColumns.UserEmail.VALUE, AttributeColumns.UserEmail.CHANGED_AT,
                AttributeColumns.UserEmail.METADATA);
    }

    @Override
    protected RowMapper<UserEmailAttribute> rowMapper() {
        return (rs, rowNum) -> UserEmailAttribute.builder()
                .id(rs.getLong(AttributeColumns.UserEmail.FK))
                .email(rs.getString(AttributeColumns.UserEmail.VALUE))
                .changedAt(rs.getTimestamp(AttributeColumns.UserEmail.CHANGED_AT).toInstant())
                .metadata(rs.getLong(AttributeColumns.UserEmail.METADATA))
                .build();
    }
}
