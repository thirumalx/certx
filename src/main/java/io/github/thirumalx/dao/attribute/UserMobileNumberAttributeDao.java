package io.github.thirumalx.dao.attribute;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.dao.columns.AttributeColumns;
import io.github.thirumalx.model.attribute.UserMobileNumberAttribute;

/**
 * @author Thirumal M
 */
@Repository
public class UserMobileNumberAttributeDao extends AttributeDao<UserMobileNumberAttribute> {

    protected UserMobileNumberAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.UserMobileNumber.TABLE, AttributeColumns.UserMobileNumber.FK,
                AttributeColumns.UserMobileNumber.VALUE, AttributeColumns.UserMobileNumber.CHANGED_AT,
                AttributeColumns.UserMobileNumber.METADATA);
    }

    @Override
    protected RowMapper<UserMobileNumberAttribute> rowMapper() {
        return (rs, rowNum) -> UserMobileNumberAttribute.builder()
                .id(rs.getLong(AttributeColumns.UserMobileNumber.FK))
                .mobileNumber(rs.getString(AttributeColumns.UserMobileNumber.VALUE))
                .changedAt(rs.getTimestamp(AttributeColumns.UserMobileNumber.CHANGED_AT).toInstant())
                .metadata(rs.getLong(AttributeColumns.UserMobileNumber.METADATA))
                .build();
    }
}
