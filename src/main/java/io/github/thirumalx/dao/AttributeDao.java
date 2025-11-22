package io.github.thirumalx.dao;

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

    protected AttributeDao(JdbcClient jdbc, String tableName, String fkColumn) {
        this.jdbc = jdbc;
        this.tableName = tableName;
        this.fkColumn = fkColumn;
    }

    public Long insert(Long anchorId, String value) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO " + tableName + 
                 "(" + fkColumn + ", value) VALUES (:id, :value)")
            .param("id", anchorId)
            .param("value", value)
            .update(keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
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
