package io.github.thirumalx.dao.generic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * @author Thirumal M
 * Tie tables usually contain only foreign keys referencing two anchor tables to establish a many-to-many relationship. 
 */
public abstract class GenericTieDao<T> {
    
    private final JdbcClient jdbc;
    private final String tableName;
    private final String[] columns;

    protected GenericTieDao(JdbcClient jdbc, String tableName, String... columns) {
        this.jdbc = jdbc;
        this.tableName = tableName;
        this.columns = columns;
    }

    public Long insert(Object... values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String cols = String.join(",", columns);
        String params = Arrays.stream(columns)
                              .map(c -> ":" + c)
                              .collect(Collectors.joining(","));

        JdbcClient.StatementSpec query = jdbc.sql(
            "INSERT INTO " + tableName + " (" + cols + ") VALUES (" + params + ")"
        );

        for (int i = 0; i < columns.length; i++) {
            query = query.param(columns[i], values[i]);
        }
        query.update();
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public List<Map<String, Object>> findByColumn(String column, Object value) {
        return jdbc.sql("SELECT * FROM " + tableName +
                        " WHERE " + column + " = :val")
                .param("val", value)
                .query()
                .listOfRows();
    }
}
