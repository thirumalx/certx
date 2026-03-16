package io.github.thirumalx.dao.view;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.columns.ViewColumns;
import io.github.thirumalx.dto.Application;

/**
 * @author Thirumal
 */
@Repository
public class ApplicationViewDao extends ViewDao<Application> {

    protected ApplicationViewDao(JdbcClient jdbc) {
        super(jdbc, Application.class);
    }

    public java.util.Optional<Application> findLatestById(Long id) {
        return findById(ViewColumns.ApplicationLatest.TABLE, ViewColumns.ApplicationLatest.ID, id);
    }

    public java.util.Optional<Application> findNowById(Long id) {
        return findById(ViewColumns.ApplicationNow.TABLE, ViewColumns.ApplicationNow.ID, id);
    }

    public java.util.Optional<Application> findNowByUniqueId(String uniqueId) {
        return jdbc.sql("SELECT * FROM " + ViewColumns.ApplicationNow.TABLE + " WHERE "
                + ViewColumns.ApplicationNow.UNIQUE_ID + " = :uniqueId")
                .param("uniqueId", uniqueId)
                .query(rowMapper())
                .optional();
    }

    public java.util.List<Application> listNow(Long status, int page, int size) {
        return jdbc.sql(
                "SELECT * FROM " + ViewColumns.ApplicationNow.TABLE + " WHERE " + ViewColumns.ApplicationNow.STATUS_ID_COL + " = :status ORDER BY " + ViewColumns.ApplicationNow.ID + " LIMIT :limit OFFSET :offset")
                .param("status", status)
                .param("limit", size)
                .param("offset", page * size)
                .query(rowMapper())
                .list();
    }

    public long countNow(Long status) {
        return jdbc.sql("SELECT count(*) FROM " + ViewColumns.ApplicationNow.TABLE + " WHERE " + ViewColumns.ApplicationNow.STATUS_ID_COL + " = :status")
                .param("status", status)
                .query(Long.class)
                .single();
    }

    @Override
    protected RowMapper<Application> rowMapper() {
        return (rs, rowNum) -> Application.builder()
                .id(rs.getLong(ViewColumns.ApplicationNow.ID))
                .applicationName(rs.getString(ViewColumns.ApplicationNow.NAME))
                .uniqueId(rs.getString(ViewColumns.ApplicationNow.UNIQUE_ID))
                .status(rs.getString(ViewColumns.ApplicationNow.STATUS))
                .build();
    }

}
