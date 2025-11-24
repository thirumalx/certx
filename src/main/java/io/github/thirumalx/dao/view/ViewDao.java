package io.github.thirumalx.dao.view;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;

public abstract class ViewDao<T> {

    private final JdbcClient jdbc;

    protected ViewDao(JdbcClient jdbc, Class<T> type) {
        this.jdbc = jdbc;
    }

    public Optional<T> findById(String viewName, String idColumn, Long id) {
        return jdbc.sql("SELECT * FROM " + viewName + " WHERE " + idColumn + " = :id")
                .param("id", id)
                .query(rowMapper())
                .optional();
    }

    protected abstract org.springframework.jdbc.core.RowMapper<T> rowMapper();
}
