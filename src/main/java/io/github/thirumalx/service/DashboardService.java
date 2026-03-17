package io.github.thirumalx.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import io.github.thirumalx.dao.columns.ViewColumns;
import io.github.thirumalx.dto.DashboardStats;
import io.github.thirumalx.model.Knot;

/**
 * @author Thirumal
 */
@Service
public class DashboardService {

        private final Logger logger = LoggerFactory.getLogger(DashboardService.class);
        private final JdbcClient jdbc;

        public DashboardService(JdbcClient jdbc) {
                this.jdbc = jdbc;
        }

        public DashboardStats getStats() {
                logger.debug("Fetching dashboard statistics");

                long totalApps = jdbc.sql("SELECT count(*) FROM certx.nAP_Application WHERE " + ViewColumns.ApplicationNow.STATUS_ID_COL + " = :active").param("active", Knot.ACTIVE).query(Long.class).single();
                long totalClients = jdbc.sql("SELECT count(*) FROM certx.nCL_Client WHERE " + ViewColumns.ClientNow.STATUS_ID_COL + " = :active").param("active", Knot.ACTIVE).query(Long.class).single();
                long totalCerts = jdbc.sql("SELECT count(*) FROM certx.nCE_Certificate WHERE " + ViewColumns.CertificateNow.STATUS_ID_COL + " = :active").param("active", Knot.ACTIVE).query(Long.class).single();

                // Status Distribution
                List<DashboardStats.StatusCount> statusDistribution = jdbc.sql(
                                "SELECT CASE " +
                                                "WHEN " + ViewColumns.CertificateNow.STATUS_ID_COL
                                                + " = :deleted THEN 'Deleted' " +
                                                "WHEN " + ViewColumns.CertificateNow.REVOKED_ON + " IS NULL AND "
                                                + ViewColumns.CertificateNow.NOT_AFTER + " < NOW() THEN 'Expired' " +
                                                "ELSE 'Active' END as status, count(*) as count " +
                                                "FROM " + ViewColumns.CertificateNow.TABLE + " GROUP BY status")
                                .param("deleted", Knot.DELETED)
                                .query((rs, rowNum) -> new DashboardStats.StatusCount(rs.getString("status"),
                                                rs.getLong("count")))
                                .list();

                // Expiry Stats (Next 6 months)
                List<DashboardStats.ExpiryCount> expiryStats = jdbc.sql(
                                "SELECT TO_CHAR(" + ViewColumns.CertificateNow.NOT_AFTER
                                                + ", 'YYYY-MM') as month, count(*) as count " +
                                                "FROM " + ViewColumns.CertificateNow.TABLE + " " +
                                                "WHERE " + ViewColumns.CertificateNow.NOT_AFTER + " >= NOW() " +
                                                "GROUP BY month ORDER BY month LIMIT 6")
                                .query((rs, rowNum) -> new DashboardStats.ExpiryCount(rs.getString("month"),
                                                rs.getLong("count")))
                                .list();

                // Top Applications (by cert count)
                List<DashboardStats.AppCount> topApps = jdbc.sql(
                                "SELECT a." + ViewColumns.ApplicationNow.NAME + " as name, count(ce.CE_ID) as count " +
                                                "FROM " + ViewColumns.ApplicationNow.TABLE + " a " +
                                                "JOIN certx.ap_serves_cl_servedby tie1 ON a.AP_ID = tie1.ap_id_serves "
                                                +
                                                "JOIN certx.ce_belongsto_cl_owns tie2 ON tie1.cl_id_servedby = tie2.cl_id_owns "
                                                +
                                                "JOIN " + ViewColumns.CertificateNow.TABLE
                                                + " ce ON tie2.ce_id_belongsto = ce.CE_ID " +
                                                "GROUP BY a.AP_ID, a.AP_NAM_Application_Name ORDER BY count DESC LIMIT 5")
                                .query((rs, rowNum) -> new DashboardStats.AppCount(rs.getString("name"),
                                                rs.getLong("count")))
                                .list();

                return new DashboardStats(totalApps, totalClients, totalCerts, statusDistribution, expiryStats,
                                topApps);
        }
}
