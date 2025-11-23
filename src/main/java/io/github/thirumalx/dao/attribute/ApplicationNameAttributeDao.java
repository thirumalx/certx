package io.github.thirumalx.dao.attribute;

import java.time.OffsetDateTime;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.thirumalx.dao.AttributeDao;
import io.github.thirumalx.model.attribute.ApplicationNameAttribute;
/**
 * @author Thirumal M
 */
@Repository
public class ApplicationNameAttributeDao extends AttributeDao<ApplicationNameAttribute> {

    protected ApplicationNameAttributeDao(JdbcClient jdbc) {
        super(jdbc, "certx.ap_nam_application_name", "ap_nam_ap_id", "ap_nam_application_name",  "ap_nam_changedat", "metadata_ap_nam");
    }

    @Override
    protected RowMapper<ApplicationNameAttribute> rowMapper() {
        return (rs, rowNum) -> new ApplicationNameAttribute(
            rs.getLong("ap_nam_ap_id"),
            rs.getString("ap_nam_application_name"),
            rs.getObject("ap_nam_changedat", OffsetDateTime.class).toInstant(),
            rs.getLong("metadata_ap_nam")
        );
    }
    
}
