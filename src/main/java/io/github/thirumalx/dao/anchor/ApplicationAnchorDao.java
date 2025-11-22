package io.github.thirumalx.dao.anchor;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AnchorDao;
import io.github.thirumalx.model.anchor.ApplicationAnchor;
/**
 * @author Thirumal M
 */
@Repository
public class ApplicationAnchorDao extends AnchorDao<ApplicationAnchor> {

    protected ApplicationAnchorDao(JdbcClient jdbc) {
        super(jdbc, "certx.ap_application", "ap_id");
    }

    @Override
    protected RowMapper<ApplicationAnchor> rowMapper() {
        return (rs, rowNum) -> new ApplicationAnchor(rs.getLong("ap_id"), rs.getLong("metadata_ap"));
    }
    
}
