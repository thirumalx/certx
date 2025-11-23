package io.github.thirumalx.dao;

import java.time.Instant;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * @author Thirumal M
 * Attribute tables usually contain attribute ID (PK), anchor ID (FK), and attribute value and optionally other metadata.
 */
public abstract class AttributeDao<T> {
    
    private final JdbcClient jdbc;
    private final String tableName;
    private final String fkColumn;
    private final String valueColumn;
    private final String changedAtColumn; // Nullable, for historized attributes
    private final String metadataColumn;

    protected AttributeDao(JdbcClient jdbc, String tableName, String fkColumn, String valueColumn, String metadataColumn) {
        this(jdbc, tableName, fkColumn, valueColumn, null, metadataColumn);
    }   

    protected AttributeDao(JdbcClient jdbc, String tableName, String fkColumn, String valueColumn, String changedAtColumn, String metadataColumn) {
        this.jdbc = jdbc;
        this.tableName = tableName;
        this.fkColumn = fkColumn;
        this.valueColumn = valueColumn;
        this.changedAtColumn = changedAtColumn;
        this.metadataColumn = metadataColumn;
    }

    public Long insert(Long anchorId, String value, Long metadata) {
        jdbc.sql("INSERT INTO " + tableName + 
                 "(" + fkColumn + ", " + valueColumn + ", " + metadataColumn 
                 + ") VALUES (:id, :value, :metadata) RETURNING " + fkColumn)
            .param("id", anchorId)
            .param("value", value)
            .param("metadata", metadata);
        return anchorId;
    }

    /** Historized Attribute */
    public Long insert(Long anchorId, String value, Instant changedAt, Long metadata) {
        if (changedAtColumn == null) {
            throw new IllegalStateException("This attribute is not historized.");
        }
        jdbc.sql("INSERT INTO " + tableName + 
                 "(" + fkColumn + ", " + valueColumn + ", " + changedAtColumn + ", " + metadataColumn 
                 + ") VALUES (:id, :value, :changedAt, :metadata) RETURNING " + fkColumn)
            .param("id", anchorId)
            .param("value", value)
            .param("changedAt", changedAt)
            .param("metadata", metadata);
        return anchorId;
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
