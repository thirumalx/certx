package io.github.thirumalx.dao.attribute;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.dao.columns.AttributeColumns;
import io.github.thirumalx.model.attribute.CertificateSerialNumberAttribute;

/**
 * @author Thirumal M
 *         Static attribute DAO for CE_SNO_Certificate_SerialNumber.
 */
@Repository
public class CertificateSerialNumberAttributeDao extends AttributeDao<CertificateSerialNumberAttribute> {

    protected CertificateSerialNumberAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.CertificateSerialNumber.TABLE, AttributeColumns.CertificateSerialNumber.FK,
                AttributeColumns.CertificateSerialNumber.VALUE, AttributeColumns.CertificateSerialNumber.METADATA);
    }

    @Override
    protected RowMapper<CertificateSerialNumberAttribute> rowMapper() {
        return (rs, rowNum) -> CertificateSerialNumberAttribute.builder()
                .id(rs.getLong(AttributeColumns.CertificateSerialNumber.FK))
                .serialNumber(rs.getString(AttributeColumns.CertificateSerialNumber.VALUE))
                .metadata(rs.getLong(AttributeColumns.CertificateSerialNumber.METADATA))
                .build();
    }

}
