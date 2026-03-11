package io.github.thirumalx.dao.tie;

import java.sql.Timestamp;
import java.time.Instant;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.TieDao;
import io.github.thirumalx.dao.columns.TieColumns;
import io.github.thirumalx.model.tie.UserClientAssigned;

/**
 * @author Thirumal M
 * DAO for historized user-client assignments.
 */
@Repository
public class UserClientAssignedTieDao extends TieDao<UserClientAssigned> {

    private final JdbcClient jdbc;

    public UserClientAssignedTieDao(JdbcClient jdbc) {
        super(jdbc,
                TieColumns.UserClientAssignedTo.TABLE,
                TieColumns.UserClientAssignedTo.ANCHOR1,
                TieColumns.UserClientAssignedTo.ANCHOR2,
                TieColumns.UserClientAssignedTo.METADATA,
                TieColumns.UserClientAssignedTo.CHANGED_AT);
        this.jdbc = jdbc;
    }

    @Override
    protected RowMapper<UserClientAssigned> rowMapper() {
        return (rs, rowNum) -> {
            Long userId = rs.getObject(TieColumns.UserClientAssignedTo.ANCHOR1) != null
                    ? rs.getLong(TieColumns.UserClientAssignedTo.ANCHOR1)
                    : null;
            Long clientId = rs.getObject(TieColumns.UserClientAssignedTo.ANCHOR2) != null
                    ? rs.getLong(TieColumns.UserClientAssignedTo.ANCHOR2)
                    : null;
            Long metadataId = rs.getObject(TieColumns.UserClientAssignedTo.METADATA) != null
                    ? rs.getLong(TieColumns.UserClientAssignedTo.METADATA)
                    : null;
            Timestamp ts = rs.getTimestamp(TieColumns.UserClientAssignedTo.CHANGED_AT);
            Instant changedAt = ts != null ? ts.toInstant() : null;
            return new UserClientAssigned(userId, clientId, metadataId, changedAt);
        };
    }

    public boolean existsAssignment(Long userId, Long clientId) {
        String sql = "SELECT 1 FROM " + TieColumns.UserClientAssignedTo.TABLE +
                " WHERE " + TieColumns.UserClientAssignedTo.ANCHOR1 + " = :userId " +
                " AND " + TieColumns.UserClientAssignedTo.ANCHOR2 + " = :clientId LIMIT 1";
        return jdbc.sql(sql)
                .param("userId", userId)
                .param("clientId", clientId)
                .query(Integer.class)
                .optional()
                .isPresent();
    }

    public boolean deleteAssignment(Long userId, Long clientId) {
        String sql = "DELETE FROM " + TieColumns.UserClientAssignedTo.TABLE +
                " WHERE " + TieColumns.UserClientAssignedTo.ANCHOR1 + " = :userId " +
                " AND " + TieColumns.UserClientAssignedTo.ANCHOR2 + " = :clientId";
        return jdbc.sql(sql)
                .param("userId", userId)
                .param("clientId", clientId)
                .update() > 0;
    }
}
