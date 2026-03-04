package io.github.thirumalx.dao.attribute;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.dao.columns.AttributeColumns;
import io.github.thirumalx.model.attribute.CertificateStatusAttribute;

/**
 * @author Thirumal M
 *         Knotted historized attribute DAO for CE_STA_Certificate_Status.
 */
@Repository
public class CertificateStatusAttributeDao extends AttributeDao<CertificateStatusAttribute> {

    private final JdbcClient jdbc;

    protected CertificateStatusAttributeDao(JdbcClient jdbc) {
        super(jdbc, AttributeColumns.CertificateStatus.TABLE, AttributeColumns.CertificateStatus.FK,
                AttributeColumns.CertificateStatus.VALUE, AttributeColumns.CertificateStatus.CHANGED_AT,
                AttributeColumns.CertificateStatus.METADATA);
        this.jdbc = jdbc;
    }

    /**
     * Specialized insert for knotted historized attribute where the value is a Long
     * (STA_ID).
     */
    public Map<String, Object> insert(Long anchorId, Long statusId, Instant changedAt, Long metadata) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO " + AttributeColumns.CertificateStatus.TABLE
                + " (" + AttributeColumns.CertificateStatus.FK
                + ", " + AttributeColumns.CertificateStatus.VALUE
                + ", " + AttributeColumns.CertificateStatus.CHANGED_AT
                + ", " + AttributeColumns.CertificateStatus.METADATA
                + ") VALUES (:id, :value, :changedAt, :metadata) RETURNING "
                + AttributeColumns.CertificateStatus.FK + ", " + AttributeColumns.CertificateStatus.CHANGED_AT)
                .param("id", anchorId)
                .param("value", statusId)
                .param("changedAt", java.sql.Timestamp.from(changedAt))
                .param("metadata", metadata)
                .update(keyHolder);
        return keyHolder.getKeys();
    }

    @Override
    protected RowMapper<CertificateStatusAttribute> rowMapper() {
        return (rs, rowNum) -> CertificateStatusAttribute.builder()
                .id(rs.getLong(AttributeColumns.CertificateStatus.FK))
                .knotId(rs.getLong(AttributeColumns.CertificateStatus.VALUE))
                .changedAt(
                        rs.getObject(AttributeColumns.CertificateStatus.CHANGED_AT, OffsetDateTime.class).toInstant())
                .metadata(rs.getLong(AttributeColumns.CertificateStatus.METADATA))
                .build();
    }

}
