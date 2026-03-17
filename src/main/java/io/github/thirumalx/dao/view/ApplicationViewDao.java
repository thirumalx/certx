package io.github.thirumalx.dao.view;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.columns.TieColumns;
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
        String sql = selectWithCounts(ViewColumns.ApplicationLatest.TABLE, "a") + " WHERE a."
                + ViewColumns.ApplicationLatest.ID + " = :id";
        return jdbc.sql(sql)
                .param("id", id)
                .query(rowMapper())
                .optional();
    }

    public java.util.Optional<Application> findNowById(Long id) {
        String sql = selectWithCounts(ViewColumns.ApplicationNow.TABLE, "a") + " WHERE a."
                + ViewColumns.ApplicationNow.ID + " = :id";
        return jdbc.sql(sql)
                .param("id", id)
                .query(rowMapper())
                .optional();
    }

    public java.util.Optional<Application> findNowByUniqueId(String uniqueId) {
        String sql = selectWithCounts(ViewColumns.ApplicationNow.TABLE, "a") + " WHERE a."
                + ViewColumns.ApplicationNow.UNIQUE_ID + " = :uniqueId";
        return jdbc.sql(sql)
                .param("uniqueId", uniqueId)
                .query(rowMapper())
                .optional();
    }

    public java.util.List<Application> listNow(Long status, int page, int size) {
        String sql = selectWithCounts(ViewColumns.ApplicationNow.TABLE, "a")
                + (status != null ? " WHERE a." + ViewColumns.ApplicationNow.STATUS_ID_COL + " = :status" : "")
                + " ORDER BY a." + ViewColumns.ApplicationNow.ID
                + " LIMIT :limit OFFSET :offset";
        var query = jdbc.sql(sql)
                .param("limit", size)
                .param("offset", page * size);
        if (status != null) {
            query = query.param("status", status);
        }
        return query.query(rowMapper()).list();
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
                .clientCount(rs.getObject("client_count") != null ? rs.getLong("client_count") : 0L)
                .certificateCount(rs.getObject("certificate_count") != null ? rs.getLong("certificate_count") : 0L)
                .build();
    }

    private String selectWithCounts(String table, String alias) {
        return "SELECT " + alias + ".*, "
                + "COALESCE(clcnt.client_count, 0) AS client_count, "
                + "COALESCE(certcnt.certificate_count, 0) AS certificate_count "
                + "FROM " + table + " " + alias + " "
                + "LEFT JOIN ("
                + "  SELECT " + TieColumns.ApplicationClientServedby.ANCHOR1 + " AS application_id, "
                + "  COUNT(DISTINCT " + TieColumns.ApplicationClientServedby.ANCHOR2 + ") AS client_count "
                + "  FROM " + TieColumns.ApplicationClientServedby.TABLE
                + "  GROUP BY " + TieColumns.ApplicationClientServedby.ANCHOR1
                + ") clcnt ON clcnt.application_id = " + alias + "." + ViewColumns.ApplicationNow.ID + " "
                + "LEFT JOIN ("
                + "  SELECT " + TieColumns.ApplicationCertificateUses.ANCHOR1 + " AS application_id, "
                + "  COUNT(DISTINCT " + TieColumns.ApplicationCertificateUses.ANCHOR2 + ") AS certificate_count "
                + "  FROM " + TieColumns.ApplicationCertificateUses.TABLE
                + "  GROUP BY " + TieColumns.ApplicationCertificateUses.ANCHOR1
                + ") certcnt ON certcnt.application_id = " + alias + "." + ViewColumns.ApplicationNow.ID;
    }

}
