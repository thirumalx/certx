package io.github.thirumalx.dao.view;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;

public abstract class ViewDao<T> {
    
    private final JdbcClient jdbc;
    private final String viewName;
    private final Class<T> type;

    protected ViewDao(JdbcClient jdbc, String viewName, Class<T> type) {
        this.jdbc = jdbc;
        this.viewName = viewName;
        this.type = type;
    }

    public Optional<T> findById(String idColumn, Long id) {
        return jdbc.sql("SELECT * FROM " + viewName + " WHERE " + idColumn + " = :id")
                .param("id", id)
                .query(type)
                .optional();
    }
}
