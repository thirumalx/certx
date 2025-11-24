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

    @Override
    protected RowMapper<Application> rowMapper() {
        return (rs, rowNum) -> Application.builder()
                .id(rs.getLong("AP_ID"))
                .applicationName(rs.getString("AP_NAM_Application_Name"))
                .build();
    }

}
