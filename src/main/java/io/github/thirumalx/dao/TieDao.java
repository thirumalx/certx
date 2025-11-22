package io.github.thirumalx.dao;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import io.github.thirumalx.model.Tie;

/**
 * @author Thirumal M
 * Tie tables usually contain only foreign keys referencing two anchor tables to establish a many-to-many relationship. 
 */
public abstract class TieDao<T extends Tie> {

    private final JdbcClient jdbc;
    private final String tableName;
    private final String[] columns;

    protected TieDao(JdbcClient jdbc, String tableName, String... columns) {
        this.jdbc = jdbc;
        this.tableName = tableName;
        this.columns = columns;
    }

    public boolean insert(Object... values) {

        if (values.length != columns.length) {
            throw new IllegalArgumentException(
                "Expected " + columns.length + " values, but got " + values.length
            );
        }

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

        return query.update() > 0;
    }

    public List<T> findByColumn(String column, Object value) {
        return jdbc.sql("SELECT * FROM " + tableName +
                        " WHERE " + column + " = :val")
                .param("val", value)
                .query(rowMapper())
                .list();
    }

    protected abstract RowMapper<T> rowMapper();
}
