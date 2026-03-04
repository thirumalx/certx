package io.github.thirumalx.dao.attribute;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.dao.columns.AttributeColumns;
import io.github.thirumalx.model.attribute.CertificateIssuedOnAttribute;

/**
 * @author Thirumal M
 *         Static attribute DAO for CE_ION_Certificate_IssuedOn.
 */
@Repository
public class CertificateIssuedOnAttributeDao extends AttributeDao<CertificateIssuedOnAttribute> {

    private final JdbcClient jdbc;

    protected CertificateIssuedOnAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.CertificateIssuedOn.TABLE, AttributeColumns.CertificateIssuedOn.FK,
                AttributeColumns.CertificateIssuedOn.VALUE, AttributeColumns.CertificateIssuedOn.METADATA);
        this.jdbc = jdbc;
    }

    /**
     * Insert for timestamp-typed static attribute.
     */
    public Map<String, Object> insert(Long anchorId, java.time.Instant issuedOn, Long metadata) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO " + AttributeColumns.CertificateIssuedOn.TABLE
                + " (" + AttributeColumns.CertificateIssuedOn.FK
                + ", " + AttributeColumns.CertificateIssuedOn.VALUE
                + ", " + AttributeColumns.CertificateIssuedOn.METADATA
                + ") VALUES (:id, :value, :metadata) RETURNING " + AttributeColumns.CertificateIssuedOn.FK)
                .param("id", anchorId)
                .param("value", java.sql.Timestamp.from(issuedOn))
                .param("metadata", metadata)
                .update(keyHolder);
        return keyHolder.getKeys();
    }

    @Override
    protected RowMapper<CertificateIssuedOnAttribute> rowMapper() {
        return (rs, rowNum) -> CertificateIssuedOnAttribute.builder()
                .id(rs.getLong(AttributeColumns.CertificateIssuedOn.FK))
                .issuedOn(rs.getObject(AttributeColumns.CertificateIssuedOn.VALUE, OffsetDateTime.class).toInstant())
                .metadata(rs.getLong(AttributeColumns.CertificateIssuedOn.METADATA))
                .build();
    }

}
