package io.github.thirumalx.dao.view;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.columns.TieColumns;
import io.github.thirumalx.dao.columns.ViewColumns;
import io.github.thirumalx.dto.Client;

/**
 * @author Thirumal
 */
@Repository
public class ClientViewDao extends ViewDao<Client> {

    protected ClientViewDao(JdbcClient jdbc) {
        super(jdbc, Client.class);
    }

    public java.util.Optional<Client> findNowById(Long id) {
        return findById(ViewColumns.ClientNow.TABLE, ViewColumns.ClientNow.ID, id);
    }

    public java.util.List<Client> listNow(Long applicationId, Long status, int page, int size) {
        String sql = "SELECT v.* FROM " + ViewColumns.ClientNow.TABLE + " v " +
                "JOIN " + TieColumns.ApplicationClientServedby.TABLE + " t ON v." + ViewColumns.ClientNow.ID + " = t."
                + TieColumns.ApplicationClientServedby.ANCHOR2 + " " +
                "WHERE t." + TieColumns.ApplicationClientServedby.ANCHOR1 + " = :applicationId AND v."
                + ViewColumns.ClientNow.STATUS_ID_COL + " = :status " +
                "ORDER BY v." + ViewColumns.ClientNow.ID + " LIMIT :limit OFFSET :offset";
        return jdbc.sql(sql)
                .param("applicationId", applicationId)
                .param("status", status)
                .param("limit", size)
                .param("offset", page * size)
                .query(rowMapper())
                .list();
    }

    public long countNow(Long applicationId, Long status) {
        String sql = "SELECT count(*) FROM " + ViewColumns.ClientNow.TABLE + " v " +
                "JOIN " + TieColumns.ApplicationClientServedby.TABLE + " t ON v." + ViewColumns.ClientNow.ID + " = t."
                + TieColumns.ApplicationClientServedby.ANCHOR2 + " " +
                "WHERE t." + TieColumns.ApplicationClientServedby.ANCHOR1 + " = :applicationId AND v."
                + ViewColumns.ClientNow.STATUS_ID_COL + " = :status";
        return jdbc.sql(sql)
                .param("applicationId", applicationId)
                .param("status", status)
                .query(Long.class)
                .single();
    }

    @Override
    protected RowMapper<Client> rowMapper() {
        return (rs, rowNum) -> Client.builder()
                .id(rs.getLong(ViewColumns.ClientNow.ID))
                .name(rs.getString(ViewColumns.ClientNow.NAME))
                .email(rs.getString(ViewColumns.ClientNow.EMAIL))
                .mobileNumber(rs.getString(ViewColumns.ClientNow.MOBILE_NUMBER))
                .status(rs.getString(ViewColumns.ClientNow.STATUS))
                .build();
    }

}
