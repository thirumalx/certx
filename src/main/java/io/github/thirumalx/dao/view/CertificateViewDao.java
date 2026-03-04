package io.github.thirumalx.dao.view;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.columns.ViewColumns;
import io.github.thirumalx.dto.Certificate;

/**
 * @author Thirumal M
 *         View DAO for querying Certificate through the nCE_Certificate and
 *         lCE_Certificate views.
 */
@Repository
public class CertificateViewDao extends ViewDao<Certificate> {

    protected CertificateViewDao(JdbcClient jdbc) {
        super(jdbc, Certificate.class);
    }

    /**
     * Find a certificate by its ID from the latest view (lCE_Certificate).
     */
    public Optional<Certificate> findLatestById(Long id) {
        return findById(ViewColumns.CertificateLatest.TABLE, ViewColumns.CertificateLatest.ID, id);
    }

    /**
     * Find a certificate by its ID from the now view (nCE_Certificate).
     */
    public Optional<Certificate> findNowById(Long id) {
        return findById(ViewColumns.CertificateNow.TABLE, ViewColumns.CertificateNow.ID, id);
    }

    /**
     * List certificates for a given client (by status) from the now view, with
     * pagination.
     */
    public List<Certificate> listNowByClient(Long clientId, int page, int size) {
        return jdbc.sql(
                "SELECT nce.* FROM " + ViewColumns.CertificateNow.TABLE + " nce"
                        + " JOIN certx.ce_belongsto_cl_owns tie"
                        + " ON tie.ce_id_belongsto = nce." + ViewColumns.CertificateNow.ID
                        + " WHERE tie.cl_id_owns = :clientId"
                        + " ORDER BY nce." + ViewColumns.CertificateNow.ID
                        + " LIMIT :limit OFFSET :offset")
                .param("clientId", clientId)
                .param("limit", size)
                .param("offset", page * size)
                .query(rowMapper())
                .list();
    }

    /**
     * Count certificates belonging to a given client from the now view.
     */
    public long countNowByClient(Long clientId) {
        return jdbc.sql(
                "SELECT count(*) FROM " + ViewColumns.CertificateNow.TABLE + " nce"
                        + " JOIN certx.ce_belongsto_cl_owns tie"
                        + " ON tie.ce_id_belongsto = nce." + ViewColumns.CertificateNow.ID
                        + " WHERE tie.cl_id_owns = :clientId")
                .param("clientId", clientId)
                .query(Long.class)
                .single();
    }

    /**
     * List all certificates with a specific status from the now view.
     */
    public List<Certificate> listNow(Long statusId, int page, int size) {
        return jdbc.sql(
                "SELECT * FROM " + ViewColumns.CertificateNow.TABLE
                        + " WHERE " + ViewColumns.CertificateNow.STATUS_ID_COL + " = :statusId"
                        + " ORDER BY " + ViewColumns.CertificateNow.ID
                        + " LIMIT :limit OFFSET :offset")
                .param("statusId", statusId)
                .param("limit", size)
                .param("offset", page * size)
                .query(rowMapper())
                .list();
    }

    /**
     * Count all certificates with a specific status from the now view.
     */
    public long countNow(Long statusId) {
        return jdbc.sql(
                "SELECT count(*) FROM " + ViewColumns.CertificateNow.TABLE
                        + " WHERE " + ViewColumns.CertificateNow.STATUS_ID_COL + " = :statusId")
                .param("statusId", statusId)
                .query(Long.class)
                .single();
    }

    @Override
    protected RowMapper<Certificate> rowMapper() {
        return (rs, rowNum) -> {
            OffsetDateTime issuedOnOdt = rs.getObject(ViewColumns.CertificateNow.ISSUED_ON, OffsetDateTime.class);
            OffsetDateTime revokedOnOdt = rs.getObject(ViewColumns.CertificateNow.REVOKED_ON, OffsetDateTime.class);
            return Certificate.builder()
                    .serialNumber(rs.getString(ViewColumns.CertificateNow.SERIAL_NUMBER))
                    .path(rs.getString(ViewColumns.CertificateNow.CERTIFICATE_PATH))
                    .issuedOn(issuedOnOdt != null ? issuedOnOdt.toLocalDateTime() : null)
                    .revokedOn(revokedOnOdt != null ? revokedOnOdt.toLocalDateTime() : null)
                    .build();
        };
    }

}
