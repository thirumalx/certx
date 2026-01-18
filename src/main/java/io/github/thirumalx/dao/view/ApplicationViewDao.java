package io.github.thirumalx.dao.view;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

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
        return findById("certx.lAP_Application", "AP_ID", id);
    }

    public java.util.Optional<Application> findNowById(Long id) {
        return findById("certx.nAP_Application", "AP_ID", id);
    }

    public java.util.List<Application> listNow(int page, int size) {
        return jdbc.sql("SELECT * FROM certx.nAP_Application ORDER BY AP_ID LIMIT :limit OFFSET :offset")
                .param("limit", size)
                .param("offset", page * size)
                .query(rowMapper())
                .list();
    }

    public long countNow() {
        return jdbc.sql("SELECT count(*) FROM certx.nAP_Application")
                .query(Long.class)
                .single();
    }

    @Override
    protected RowMapper<Application> rowMapper() {
        return (rs, rowNum) -> Application.builder()
                .id(rs.getLong("AP_ID"))
                .applicationName(rs.getString("AP_NAM_Application_Name"))
                .uniqueId(rs.getString("AP_UID_Application_UniqueId"))
                .build();
    }

}
