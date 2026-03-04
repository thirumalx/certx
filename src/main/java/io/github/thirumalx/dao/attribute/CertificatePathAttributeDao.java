package io.github.thirumalx.dao.attribute;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.dao.columns.AttributeColumns;
import io.github.thirumalx.model.attribute.CertificatePathAttribute;

/**
 * @author Thirumal M
 *         Static attribute DAO for CE_CEP_Certificate_CertificatePath.
 */
@Repository
public class CertificatePathAttributeDao extends AttributeDao<CertificatePathAttribute> {

    protected CertificatePathAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.CertificatePath.TABLE, AttributeColumns.CertificatePath.FK,
                AttributeColumns.CertificatePath.VALUE, AttributeColumns.CertificatePath.METADATA);
    }

    @Override
    protected RowMapper<CertificatePathAttribute> rowMapper() {
        return (rs, rowNum) -> CertificatePathAttribute.builder()
                .id(rs.getLong(AttributeColumns.CertificatePath.FK))
                .certificatePath(rs.getString(AttributeColumns.CertificatePath.VALUE))
                .metadata(rs.getLong(AttributeColumns.CertificatePath.METADATA))
                .build();
    }

}
