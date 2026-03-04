package io.github.thirumalx.dao.anchor;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AnchorDao;
import io.github.thirumalx.dao.columns.AnchorColumns;
import io.github.thirumalx.model.anchor.CertificateAnchor;

/**
 * @author Thirumal M
 */
@Repository
public class CertificateAnchorDao extends AnchorDao<CertificateAnchor> {

    protected CertificateAnchorDao(JdbcClient jdbc) {
        super(jdbc, AnchorColumns.Certificate.TABLE, AnchorColumns.Certificate.ID, AnchorColumns.Certificate.METADATA);
    }

    @Override
    protected RowMapper<CertificateAnchor> rowMapper() {
        return (rs, rowNum) -> new CertificateAnchor(rs.getLong(AnchorColumns.Certificate.ID),
                rs.getLong(AnchorColumns.Certificate.METADATA));
    }

}
