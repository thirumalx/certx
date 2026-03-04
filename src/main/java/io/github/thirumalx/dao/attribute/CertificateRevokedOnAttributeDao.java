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
import io.github.thirumalx.model.attribute.CertificateRevokedOnAttribute;

/**
 * @author Thirumal M
 *         Static attribute DAO for CE_RON_Certificate_RevokedOn.
 */
@Repository
public class CertificateRevokedOnAttributeDao extends AttributeDao<CertificateRevokedOnAttribute> {

    private final JdbcClient jdbc;

    protected CertificateRevokedOnAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.CertificateRevokedOn.TABLE, AttributeColumns.CertificateRevokedOn.FK,
                AttributeColumns.CertificateRevokedOn.VALUE, AttributeColumns.CertificateRevokedOn.METADATA);
        this.jdbc = jdbc;
    }

    /**
     * Insert for timestamp-typed static attribute.
     */
    public Map<String, Object> insert(Long anchorId, java.time.Instant revokedOn, Long metadata) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO " + AttributeColumns.CertificateRevokedOn.TABLE
                + " (" + AttributeColumns.CertificateRevokedOn.FK
                + ", " + AttributeColumns.CertificateRevokedOn.VALUE
                + ", " + AttributeColumns.CertificateRevokedOn.METADATA
                + ") VALUES (:id, :value, :metadata) RETURNING " + AttributeColumns.CertificateRevokedOn.FK)
                .param("id", anchorId)
                .param("value", java.sql.Timestamp.from(revokedOn))
                .param("metadata", metadata)
                .update(keyHolder);
        return keyHolder.getKeys();
    }

    @Override
    protected RowMapper<CertificateRevokedOnAttribute> rowMapper() {
        return (rs, rowNum) -> CertificateRevokedOnAttribute.builder()
                .id(rs.getLong(AttributeColumns.CertificateRevokedOn.FK))
                .revokedOn(rs.getObject(AttributeColumns.CertificateRevokedOn.VALUE, OffsetDateTime.class).toInstant())
                .metadata(rs.getLong(AttributeColumns.CertificateRevokedOn.METADATA))
                .build();
    }

}
