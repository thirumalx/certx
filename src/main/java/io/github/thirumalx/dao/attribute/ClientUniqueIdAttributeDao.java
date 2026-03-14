package io.github.thirumalx.dao.attribute;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.dao.columns.AttributeColumns;
import io.github.thirumalx.model.attribute.ClientUniqueIdAttribute;

/**
 * @author Thirumal M
 */
@Repository
public class ClientUniqueIdAttributeDao extends AttributeDao<ClientUniqueIdAttribute> {

    protected ClientUniqueIdAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.ClientUniqueId.TABLE, AttributeColumns.ClientUniqueId.FK,
                AttributeColumns.ClientUniqueId.VALUE, AttributeColumns.ClientUniqueId.METADATA);
    }

    @Override
    protected RowMapper<ClientUniqueIdAttribute> rowMapper() {
        return (rs, rowNum) -> ClientUniqueIdAttribute.builder()
                .id(rs.getLong(AttributeColumns.ClientUniqueId.FK))
                .uniqueId(rs.getString(AttributeColumns.ClientUniqueId.VALUE))
                .metadata(rs.getLong(AttributeColumns.ClientUniqueId.METADATA))
                .build();
    }
}
