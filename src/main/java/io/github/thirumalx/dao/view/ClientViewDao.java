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
        String sql = selectWithCounts(null) + " WHERE v." + ViewColumns.ClientNow.ID + " = :id";
        return jdbc.sql(sql)
                .param("id", id)
                .query(rowMapper())
                .optional();
    }

    public java.util.Optional<Client> findNowByUniqueId(String uniqueId) {
        String sql = selectWithCounts(null) + " WHERE v." + ViewColumns.ClientNow.UNIQUE_ID + " = :uniqueId";
        return jdbc.sql(sql)
                .param("uniqueId", uniqueId)
                .query(rowMapper())
                .optional();
    }

    public java.util.List<Client> listNow(Long applicationId, Long status, int page, int size) {
        String sql = selectWithCounts(applicationId) +
                " JOIN " + TieColumns.ApplicationClientServedby.TABLE + " t ON v." + ViewColumns.ClientNow.ID
                + " = t." + TieColumns.ApplicationClientServedby.ANCHOR2 + " " +
                "WHERE t." + TieColumns.ApplicationClientServedby.ANCHOR1 + " = :applicationId" +
                (status != null ? " AND v." + ViewColumns.ClientNow.STATUS_ID_COL + " = :status " : "") +
                " ORDER BY v." + ViewColumns.ClientNow.ID + " LIMIT :limit OFFSET :offset";
        var query = jdbc.sql(sql)
                .param("applicationId", applicationId)
                .param("limit", size)
                .param("offset", page * size);
        if (status != null) {
            query = query.param("status", status);
        }
        return query.query(rowMapper()).list();
    }

    public long countNow(Long applicationId, Long status) {
        String sql = "SELECT count(*) FROM " + ViewColumns.ClientNow.TABLE + " v " +
                "JOIN " + TieColumns.ApplicationClientServedby.TABLE + " t ON v." + ViewColumns.ClientNow.ID + " = t."
                + TieColumns.ApplicationClientServedby.ANCHOR2 + " " +
                "WHERE t." + TieColumns.ApplicationClientServedby.ANCHOR1 + " = :applicationId" +
                (status != null ? " AND v." + ViewColumns.ClientNow.STATUS_ID_COL + " = :status " : "");
        var query = jdbc.sql(sql)
                .param("applicationId", applicationId);
        if (status != null) {
            query = query.param("status", status);
        }
        return query.query(Long.class)
                .single();
    }

    @Override
    protected RowMapper<Client> rowMapper() {
        return (rs, rowNum) -> Client.builder()
                .id(rs.getLong(ViewColumns.ClientNow.ID))
                .uniqueId(rs.getString(ViewColumns.ClientNow.UNIQUE_ID))
                .name(rs.getString(ViewColumns.ClientNow.NAME))
                .email(rs.getString(ViewColumns.ClientNow.EMAIL))
                .mobileNumber(rs.getString(ViewColumns.ClientNow.MOBILE_NUMBER))
                .status(rs.getString(ViewColumns.ClientNow.STATUS))
                .assignedUserCount(rs.getObject("assigned_user_count") != null ? rs.getInt("assigned_user_count") : 0)
                .certificateCount(rs.getObject("certificate_count") != null ? rs.getInt("certificate_count") : 0)
                .build();
    }

    private String selectWithCounts(Long applicationId) {
        StringBuilder sql = new StringBuilder("SELECT v.*, " +
                "COALESCE(ucnt.assigned_user_count, 0) AS assigned_user_count, " +
                "COALESCE(ccnt.certificate_count, 0) AS certificate_count " +
                "FROM " + ViewColumns.ClientNow.TABLE + " v " +
                "LEFT JOIN ( " +
                "   SELECT " + TieColumns.UserClientAssignedTo.ANCHOR2 + " AS client_id, " +
                "          COUNT(DISTINCT " + TieColumns.UserClientAssignedTo.ANCHOR1 + ") AS assigned_user_count " +
                "   FROM " + TieColumns.UserClientAssignedTo.TABLE + " " +
                "   GROUP BY " + TieColumns.UserClientAssignedTo.ANCHOR2 + " " +
                ") ucnt ON ucnt.client_id = v." + ViewColumns.ClientNow.ID + " " +
                "LEFT JOIN (" +
                "   SELECT tie." + TieColumns.CertificateClientOwns.ANCHOR2 + " AS client_id, " +
                "          COUNT(DISTINCT tie." + TieColumns.CertificateClientOwns.ANCHOR1 + ") AS certificate_count " +
                "   FROM " + TieColumns.CertificateClientOwns.TABLE + " tie " +
                "   JOIN " + ViewColumns.CertificateNow.TABLE + " nce ON nce." + ViewColumns.CertificateNow.ID + " = tie." + TieColumns.CertificateClientOwns.ANCHOR1);

        if (applicationId != null) {
            sql.append(" JOIN " + TieColumns.ApplicationCertificateUses.TABLE + " apce ON apce." + TieColumns.ApplicationCertificateUses.ANCHOR2 + " = nce." + ViewColumns.CertificateNow.ID + " AND apce." + TieColumns.ApplicationCertificateUses.ANCHOR1 + " = :applicationId");
        }

        sql.append("   WHERE nce." + ViewColumns.CertificateNow.REVOKED_ON + " IS NULL"
                + "   AND (nce." + ViewColumns.CertificateNow.NOT_AFTER + " IS NULL OR nce." + ViewColumns.CertificateNow.NOT_AFTER + " > NOW())"
                + "   AND nce." + ViewColumns.CertificateNow.STATUS + " <> 'DELETED' " +
                "   GROUP BY tie." + TieColumns.CertificateClientOwns.ANCHOR2 + " " +
                ") ccnt ON ccnt.client_id = v." + ViewColumns.ClientNow.ID + " ");

        return sql.toString();
    }

    public java.util.List<Client> listByCertificate(Long certificateId) {
        String sql = selectWithCounts(null) +
                " JOIN " + TieColumns.CertificateClientOwns.TABLE + " tie ON v." + ViewColumns.ClientNow.ID + " = tie." + TieColumns.CertificateClientOwns.ANCHOR2 +
                " WHERE tie." + TieColumns.CertificateClientOwns.ANCHOR1 + " = :certificateId";
        return jdbc.sql(sql)
                .param("certificateId", certificateId)
                .query(rowMapper())
                .list();
    }
}
