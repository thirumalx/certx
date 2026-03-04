package io.github.thirumalx.dao.anchor;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AnchorDao;
import io.github.thirumalx.dao.columns.AnchorColumns;
import io.github.thirumalx.model.anchor.ClientAnchor;

/**
 * @author Thirumal M
 */
@Repository
public class ClientAnchorDao extends AnchorDao<ClientAnchor> {

    protected ClientAnchorDao(JdbcClient jdbc) {
        super(jdbc, AnchorColumns.Client.TABLE, AnchorColumns.Client.ID, AnchorColumns.Client.METADATA);
    }

    @Override
    protected RowMapper<ClientAnchor> rowMapper() {
        return (rs, rowNum) -> new ClientAnchor(rs.getLong(AnchorColumns.Client.ID),
                rs.getLong(AnchorColumns.Client.METADATA));
    }

}
