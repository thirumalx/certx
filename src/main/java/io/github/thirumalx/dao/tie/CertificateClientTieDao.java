package io.github.thirumalx.dao.tie;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.TieDao;
import io.github.thirumalx.dao.columns.TieColumns;
import io.github.thirumalx.model.tie.CertificateClient;

/**
 * @author Thirumal M
 *         DAO for the static tie CE_belongsTo_CL_owns linking a Certificate to
 *         its Client owner.
 */
@Repository
public class CertificateClientTieDao extends TieDao<CertificateClient> {

    public CertificateClientTieDao(JdbcClient jdbc) {
        super(jdbc, TieColumns.CertificateClientOwns.TABLE,
                TieColumns.CertificateClientOwns.ANCHOR1,
                TieColumns.CertificateClientOwns.ANCHOR2,
                TieColumns.CertificateClientOwns.METADATA);
    }

    @Override
    protected RowMapper<CertificateClient> rowMapper() {
        return (rs, rowNum) -> {
            Long certificateId = rs.getObject(TieColumns.CertificateClientOwns.ANCHOR1) != null
                    ? rs.getLong(TieColumns.CertificateClientOwns.ANCHOR1)
                    : null;
            Long clientId = rs.getObject(TieColumns.CertificateClientOwns.ANCHOR2) != null
                    ? rs.getLong(TieColumns.CertificateClientOwns.ANCHOR2)
                    : null;
            Long metadataId = rs.getObject(TieColumns.CertificateClientOwns.METADATA) != null
                    ? rs.getLong(TieColumns.CertificateClientOwns.METADATA)
                    : null;
            return new CertificateClient(certificateId, clientId, metadataId);
        };
    }

}
