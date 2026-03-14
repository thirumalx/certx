package io.github.thirumalx.dao.tie;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.TieDao;
import io.github.thirumalx.dao.columns.TieColumns;
import io.github.thirumalx.model.tie.ApplicationCertificate;

/**
 * @author Thirumal M
 * DAO for the static tie AP_uses_CE_isusedby linking a Certificate to
 * its Application.
 */
@Repository
public class ApplicationCertificateTieDao extends TieDao<ApplicationCertificate> {

    public ApplicationCertificateTieDao(JdbcClient jdbc) {
        super(jdbc,
                TieColumns.ApplicationCertificateUses.TABLE,
                TieColumns.ApplicationCertificateUses.ANCHOR1,
                TieColumns.ApplicationCertificateUses.ANCHOR2,
                TieColumns.ApplicationCertificateUses.METADATA);
    }

    @Override
    protected RowMapper<ApplicationCertificate> rowMapper() {
        return (rs, rowNum) -> {
            Long applicationId = rs.getObject(TieColumns.ApplicationCertificateUses.ANCHOR1) != null
                    ? rs.getLong(TieColumns.ApplicationCertificateUses.ANCHOR1)
                    : null;
            Long certificateId = rs.getObject(TieColumns.ApplicationCertificateUses.ANCHOR2) != null
                    ? rs.getLong(TieColumns.ApplicationCertificateUses.ANCHOR2)
                    : null;
            Long metadataId = rs.getObject(TieColumns.ApplicationCertificateUses.METADATA) != null
                    ? rs.getLong(TieColumns.ApplicationCertificateUses.METADATA)
                    : null;
            return new ApplicationCertificate(applicationId, certificateId, metadataId);
        };
    }
}
