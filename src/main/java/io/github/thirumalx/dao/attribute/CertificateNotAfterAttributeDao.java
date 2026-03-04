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
import io.github.thirumalx.model.attribute.CertificateNotAfterAttribute;

/**
 * @author Thirumal M
 *         Static attribute DAO for CE_NAF_Certificate_NotAfter.
 */
@Repository
public class CertificateNotAfterAttributeDao extends AttributeDao<CertificateNotAfterAttribute> {

    private final JdbcClient jdbc;

    protected CertificateNotAfterAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.CertificateNotAfter.TABLE, AttributeColumns.CertificateNotAfter.FK,
                AttributeColumns.CertificateNotAfter.VALUE, AttributeColumns.CertificateNotAfter.METADATA);
        this.jdbc = jdbc;
    }

    /**
     * Insert for timestamp-typed static attribute.
     */
    public Map<String, Object> insert(Long anchorId, java.time.Instant notAfter, Long metadata) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO " + AttributeColumns.CertificateNotAfter.TABLE
                + " (" + AttributeColumns.CertificateNotAfter.FK
                + ", " + AttributeColumns.CertificateNotAfter.VALUE
                + ", " + AttributeColumns.CertificateNotAfter.METADATA
                + ") VALUES (:id, :value, :metadata) RETURNING " + AttributeColumns.CertificateNotAfter.FK)
                .param("id", anchorId)
                .param("value", java.sql.Timestamp.from(notAfter))
                .param("metadata", metadata)
                .update(keyHolder);
        return keyHolder.getKeys();
    }

    @Override
    protected RowMapper<CertificateNotAfterAttribute> rowMapper() {
        return (rs, rowNum) -> CertificateNotAfterAttribute.builder()
                .id(rs.getLong(AttributeColumns.CertificateNotAfter.FK))
                .notAfter(rs.getObject(AttributeColumns.CertificateNotAfter.VALUE, OffsetDateTime.class).toInstant())
                .metadata(rs.getLong(AttributeColumns.CertificateNotAfter.METADATA))
                .build();
    }

}
