package io.github.thirumalx.dao.tie;

import java.sql.Timestamp;
import java.time.Instant;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.TieDao;
import io.github.thirumalx.dao.columns.TieColumns;
import io.github.thirumalx.model.tie.ApplicationClient;

/**
 * @author Thirumal M
 * Data Access Object for the association between Clients and Applications.
 */
@Repository
public class ApplicationClientTieDao extends TieDao<ApplicationClient> {

    private final JdbcClient jdbc;

    public ApplicationClientTieDao(JdbcClient jdbc) {
        super(jdbc, TieColumns.ApplicationClientServedby.TABLE, TieColumns.ApplicationClientServedby.ANCHOR1, TieColumns.ApplicationClientServedby.ANCHOR2, TieColumns.ApplicationClientServedby.METADATA, TieColumns.ApplicationClientServedby.CHANGED_AT);
        this.jdbc = jdbc;
    }

    @Override
    protected RowMapper<ApplicationClient> rowMapper() {
        return (rs, rowNum) -> {
            Long applicationId = rs.getObject(TieColumns.ApplicationClientServedby.ANCHOR1) != null ? rs.getLong(TieColumns.ApplicationClientServedby.ANCHOR1) : null;
            Long clientId = rs.getObject(TieColumns.ApplicationClientServedby.ANCHOR2) != null ? rs.getLong(TieColumns.ApplicationClientServedby.ANCHOR2) : null;
            Long metadataId = rs.getObject(TieColumns.ApplicationClientServedby.METADATA) != null ? rs.getLong(TieColumns.ApplicationClientServedby.METADATA) : null;
            Timestamp ts = rs.getTimestamp(TieColumns.ApplicationClientServedby.CHANGED_AT);
            Instant changedAt = ts != null ? ts.toInstant() : null;
            return new ApplicationClient(applicationId, clientId, metadataId, changedAt);
        };
    }

    public boolean existsAssignment(Long applicationId, Long clientId) {
        String sql = "SELECT 1 FROM " + TieColumns.ApplicationClientServedby.TABLE +
                " WHERE " + TieColumns.ApplicationClientServedby.ANCHOR1 + " = :applicationId " +
                " AND " + TieColumns.ApplicationClientServedby.ANCHOR2 + " = :clientId LIMIT 1";
        return jdbc.sql(sql)
                .param("applicationId", applicationId)
                .param("clientId", clientId)
                .query(Integer.class)
                .optional()
                .isPresent();
    }
    
}
