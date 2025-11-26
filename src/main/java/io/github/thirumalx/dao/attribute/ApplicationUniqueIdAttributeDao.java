package io.github.thirumalx.dao.attribute;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.model.attribute.ApplicationUniqueIdAttribute;

/**
 * @author Thirumal
 */
@Repository
public class ApplicationUniqueIdAttributeDao extends AttributeDao<ApplicationUniqueIdAttribute> {

    protected ApplicationUniqueIdAttributeDao(JdbcClient jdbc) {
        super(jdbc, "certx.ap_uid_application_uniqueid", "ap_uid_ap_id", "ap_uid_application_uniqueid",
                "metadata_ap_uid");
    }

    @Override
    protected RowMapper<ApplicationUniqueIdAttribute> rowMapper() {
        return (rs, rowNum) -> new ApplicationUniqueIdAttribute(
                rs.getLong("ap_uid_ap_id"),
                rs.getString("ap_uid_application_uniqueid"),
                rs.getLong("metadata_ap_uid"));
    }

}
