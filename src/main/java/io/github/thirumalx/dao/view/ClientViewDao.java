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
        String sql = selectWithAssignedUser() + " WHERE v." + ViewColumns.ClientNow.ID + " = :id";
        return jdbc.sql(sql)
                .param("id", id)
                .query(rowMapper())
                .optional();
    }

    public java.util.List<Client> listNow(Long applicationId, Long status, int page, int size) {
        String sql = selectWithAssignedUser() +
                " JOIN " + TieColumns.ApplicationClientServedby.TABLE + " t ON v." + ViewColumns.ClientNow.ID
                + " = t." + TieColumns.ApplicationClientServedby.ANCHOR2 + " " +
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
                .assignedUserCount(rs.getObject("assigned_user_count") != null ? rs.getInt("assigned_user_count") : 0)
                .build();
    }

    private String selectWithAssignedUser() {
        return "SELECT v.*, COALESCE(ucnt.assigned_user_count, 0) AS assigned_user_count " +
                "FROM " + ViewColumns.ClientNow.TABLE + " v " +
                "LEFT JOIN ( " +
                "   SELECT " + TieColumns.UserClientAssignedTo.ANCHOR2 + " AS client_id, " +
                "       COUNT(DISTINCT " + TieColumns.UserClientAssignedTo.ANCHOR1 + ") AS assigned_user_count " +
                "   FROM " + TieColumns.UserClientAssignedTo.TABLE + " " +
                "   GROUP BY " + TieColumns.UserClientAssignedTo.ANCHOR2 + " " +
                ") ucnt ON ucnt.client_id = v." + ViewColumns.ClientNow.ID + " ";
    }
}
