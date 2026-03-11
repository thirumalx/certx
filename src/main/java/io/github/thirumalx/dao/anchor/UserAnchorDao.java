package io.github.thirumalx.dao.anchor;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AnchorDao;
import io.github.thirumalx.dao.columns.AnchorColumns;
import io.github.thirumalx.model.anchor.UserAnchor;

/**
 * @author Thirumal M
 */
@Repository
public class UserAnchorDao extends AnchorDao<UserAnchor> {

    protected UserAnchorDao(JdbcClient jdbc) {
        super(jdbc, AnchorColumns.User.TABLE, AnchorColumns.User.ID, AnchorColumns.User.METADATA);
    }

    @Override
    protected RowMapper<UserAnchor> rowMapper() {
        return (rs, rowNum) -> new UserAnchor(rs.getLong(AnchorColumns.User.ID),
                rs.getLong(AnchorColumns.User.METADATA));
    }
}
