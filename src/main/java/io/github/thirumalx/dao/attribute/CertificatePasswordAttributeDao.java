package io.github.thirumalx.dao.attribute;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.dao.columns.AttributeColumns;
import io.github.thirumalx.model.attribute.CertificatePasswordAttribute;

/**
 * @author Thirumal M
 *         Static attribute DAO for CE_PAS_Certificate_Password.
 */
@Repository
public class CertificatePasswordAttributeDao extends AttributeDao<CertificatePasswordAttribute> {

    protected CertificatePasswordAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.CertificatePassword.TABLE, AttributeColumns.CertificatePassword.FK,
                AttributeColumns.CertificatePassword.VALUE, AttributeColumns.CertificatePassword.METADATA);
    }

    @Override
    protected RowMapper<CertificatePasswordAttribute> rowMapper() {
        return (rs, rowNum) -> CertificatePasswordAttribute.builder()
                .id(rs.getLong(AttributeColumns.CertificatePassword.FK))
                .certificatePassword(rs.getString(AttributeColumns.CertificatePassword.VALUE))
                .metadata(rs.getLong(AttributeColumns.CertificatePassword.METADATA))
                .build();
    }

}
