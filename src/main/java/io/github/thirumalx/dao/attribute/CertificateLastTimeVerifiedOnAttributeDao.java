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
import io.github.thirumalx.model.attribute.CertificateLastTimeVerifiedOnAttribute;

/**
 * @author Thirumal M
 *         Static attribute DAO for CE_LTV_Certificate_LastTimeVerifiedOn.
 */
@Repository
public class CertificateLastTimeVerifiedOnAttributeDao extends AttributeDao<CertificateLastTimeVerifiedOnAttribute> {

    private final JdbcClient jdbc;

    protected CertificateLastTimeVerifiedOnAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.CertificateLastTimeVerifiedOn.TABLE,
                AttributeColumns.CertificateLastTimeVerifiedOn.FK,
                AttributeColumns.CertificateLastTimeVerifiedOn.VALUE,
                AttributeColumns.CertificateLastTimeVerifiedOn.METADATA);
        this.jdbc = jdbc;
    }

    /**
     * Insert for timestamp-typed static attribute.
     */
    public Map<String, Object> insert(Long anchorId, java.time.Instant lastTimeVerifiedOn, Long metadata) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO " + AttributeColumns.CertificateLastTimeVerifiedOn.TABLE
                + " (" + AttributeColumns.CertificateLastTimeVerifiedOn.FK
                + ", " + AttributeColumns.CertificateLastTimeVerifiedOn.VALUE
                + ", " + AttributeColumns.CertificateLastTimeVerifiedOn.METADATA
                + ") VALUES (:id, :value, :metadata)"
                + " ON CONFLICT (" + AttributeColumns.CertificateLastTimeVerifiedOn.FK + ")"
                + " DO UPDATE SET "
                + AttributeColumns.CertificateLastTimeVerifiedOn.VALUE + " = EXCLUDED."
                + AttributeColumns.CertificateLastTimeVerifiedOn.VALUE + ", "
                + AttributeColumns.CertificateLastTimeVerifiedOn.METADATA + " = EXCLUDED."
                + AttributeColumns.CertificateLastTimeVerifiedOn.METADATA
                + " RETURNING " + AttributeColumns.CertificateLastTimeVerifiedOn.FK)
                .param("id", anchorId)
                .param("value", java.sql.Timestamp.from(lastTimeVerifiedOn))
                .param("metadata", metadata)
                .update(keyHolder);
        return keyHolder.getKeys();
    }

    @Override
    protected RowMapper<CertificateLastTimeVerifiedOnAttribute> rowMapper() {
        return (rs, rowNum) -> CertificateLastTimeVerifiedOnAttribute.builder()
                .id(rs.getLong(AttributeColumns.CertificateLastTimeVerifiedOn.FK))
                .lastTimeVerifiedOn(
                        rs.getObject(AttributeColumns.CertificateLastTimeVerifiedOn.VALUE, OffsetDateTime.class)
                                .toInstant())
                .metadata(rs.getLong(AttributeColumns.CertificateLastTimeVerifiedOn.METADATA))
                .build();
    }

}
