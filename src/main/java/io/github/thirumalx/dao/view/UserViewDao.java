package io.github.thirumalx.dao.view;

import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.columns.TieColumns;
import io.github.thirumalx.dao.columns.ViewColumns;
import io.github.thirumalx.dto.User;

/**
 * @author Thirumal
 */
@Repository
public class UserViewDao extends ViewDao<User> {

    protected UserViewDao(JdbcClient jdbc) {
        super(jdbc, User.class);
    }

    public Optional<User> findNowById(Long id) {
        return findById(ViewColumns.UserNow.TABLE, ViewColumns.UserNow.ID, id);
    }

    public Optional<User> findNowByEmail(String email) {
        String sql = "SELECT * FROM " + ViewColumns.UserNow.TABLE + " WHERE LOWER(" + ViewColumns.UserNow.EMAIL
                + ") = LOWER(:email)";
        return jdbc.sql(sql)
                .param("email", email)
                .query(rowMapper())
                .optional();
    }

    public Optional<User> findLatestAssignedToClient(Long clientId) {
        String sql = "SELECT u.* FROM " + ViewColumns.UserNow.TABLE + " u " +
                "JOIN ( " +
                "   SELECT " + TieColumns.UserClientAssignedTo.ANCHOR1 + " " +
                "   FROM " + TieColumns.UserClientAssignedTo.TABLE + " " +
                "   WHERE " + TieColumns.UserClientAssignedTo.ANCHOR2 + " = :clientId " +
                "   ORDER BY " + TieColumns.UserClientAssignedTo.CHANGED_AT + " DESC " +
                "   LIMIT 1 " +
                ") ucl ON ucl." + TieColumns.UserClientAssignedTo.ANCHOR1 + " = u." + ViewColumns.UserNow.ID;
        return jdbc.sql(sql)
                .param("clientId", clientId)
                .query(rowMapper())
                .optional();
    }

    public java.util.List<User> listAssignedToClient(Long clientId) {
        String sql = "SELECT DISTINCT ON (u." + ViewColumns.UserNow.ID + ") u.* FROM " + ViewColumns.UserNow.TABLE
                + " u " +
                "JOIN " + TieColumns.UserClientAssignedTo.TABLE + " t ON t."
                + TieColumns.UserClientAssignedTo.ANCHOR1 + " = u." + ViewColumns.UserNow.ID + " " +
                "WHERE t." + TieColumns.UserClientAssignedTo.ANCHOR2 + " = :clientId " +
                "ORDER BY u." + ViewColumns.UserNow.ID + ", " + TieColumns.UserClientAssignedTo.CHANGED_AT + " DESC";
        return jdbc.sql(sql)
                .param("clientId", clientId)
                .query(rowMapper())
                .list();
    }

    @Override
    protected RowMapper<User> rowMapper() {
        return (rs, rowNum) -> User.builder()
                .id(rs.getLong(ViewColumns.UserNow.ID))
                .userId(rs.getString(ViewColumns.UserNow.USER_ID))
                .name(rs.getString(ViewColumns.UserNow.NAME))
                .email(rs.getString(ViewColumns.UserNow.EMAIL))
                .mobileNumber(rs.getString(ViewColumns.UserNow.MOBILE_NUMBER))
                .build();
    }
}
