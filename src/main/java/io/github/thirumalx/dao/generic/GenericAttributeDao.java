package io.github.thirumalx.dao.generic;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * @author Thirumal M
 * Attribute tables usually contain attribute ID (PK), anchor ID (FK), and attribute value and optionally other metadata.
 */
public abstract class GenericAttributeDao<T> {
    
    private final JdbcClient jdbc;
    private final String tableName;
    private final String fkColumn;
    private final Class<T> type;

    protected GenericAttributeDao(JdbcClient jdbc, String tableName, String fkColumn, Class<T> type) {
        this.jdbc = jdbc;
        this.tableName = tableName;
        this.fkColumn = fkColumn;
        this.type = type;
    }

    public Long insert(Long anchorId, String value) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO " + tableName + 
                 "(" + fkColumn + ", value) VALUES (:id, :value)")
            .param("id", anchorId)
            .param("value", value)
            .update();
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public List<T> findByAnchorId(Long anchorId) {
        return jdbc.sql("SELECT * FROM " + tableName +
                        " WHERE " + fkColumn + " = :id")
                .param("id", anchorId)
                .query(type)
                .list();
    }
}
