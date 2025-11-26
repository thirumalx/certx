package io.github.thirumalx.dao;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * @author Thirumal M
 *         Attribute tables usually contain attribute ID (PK), anchor ID (FK),
 *         and attribute value and optionally other metadata.
 */
public abstract class AttributeDao<T> {

    private final JdbcClient jdbc;
    private final String tableName;
    private final String fkColumn;
    private final String valueColumn;
    private final String changedAtColumn; // Nullable, for historized attributes
    private final String metadataColumn;

    protected AttributeDao(JdbcClient jdbc, String tableName, String fkColumn, String valueColumn,
            String metadataColumn) {
        this(jdbc, tableName, fkColumn, valueColumn, null, metadataColumn);
    }

    protected AttributeDao(JdbcClient jdbc, String tableName, String fkColumn, String valueColumn,
            String changedAtColumn, String metadataColumn) {
        this.jdbc = jdbc;
        this.tableName = tableName;
        this.fkColumn = fkColumn;
        this.valueColumn = valueColumn;
        this.changedAtColumn = changedAtColumn;
        this.metadataColumn = metadataColumn;
    }

    // ------------------------------------------------
    // INSERT SIMPLE ATTRIBUTE (non-historized)
    // ------------------------------------------------
    public Map<String, Object> insert(Long anchorId, String value, Long metadata) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO " + tableName +
                "(" + fkColumn + ", " + valueColumn + ", " + metadataColumn
                + ") VALUES (:id, :value, :metadata) RETURNING " + fkColumn + "," + metadataColumn)
                .param("id", anchorId)
                .param("value", value)
                .param("metadata", metadata)
                .update(keyHolder);
        return keyHolder.getKeys(); // returns multiple keys
    }

    // ------------------------------------------------
    // INSERT HISTORIZED ATTRIBUTE
    // ------------------------------------------------
    public Map<String, Object> insert(Long anchorId, String value, Instant changedAt, Long metadata) {
        if (changedAtColumn == null) {
            throw new IllegalStateException("This attribute is not historized.");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO " + tableName +
                "(" + fkColumn + ", " + valueColumn + ", " + changedAtColumn + ", " + metadataColumn
                + ") VALUES (:id, :value, :changedAt, :metadata) RETURNING " + fkColumn + ", " + changedAtColumn)
                .param("id", anchorId)
                .param("value", value)
                .param("changedAt", java.sql.Timestamp.from(changedAt))
                .param("metadata", metadata)
                .update(keyHolder);
        return keyHolder.getKeys(); // <-- contains BOTH PK columns
    }

    public List<T> findByAnchorId(Long anchorId) {
        return jdbc.sql("SELECT * FROM " + tableName +
                " WHERE " + fkColumn + " = :id")
                .param("id", anchorId)
                .query(rowMapper())
                .list();
    }

    protected abstract RowMapper<T> rowMapper();
}
