package io.github.thirumalx.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import io.github.thirumalx.model.GenericKnot;

/**
 * @author Thirumal
 */
@Service
public class KnotService {

    private final Logger logger = LoggerFactory.getLogger(KnotService.class);

    private final JdbcClient jdbcClient;

    public KnotService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<GenericKnot> listKnots(String knotName) {
        logger.info("Listing knots for knotName: {}", knotName);
        if (!knotName.matches("^[a-zA-Z_]+$")) {
            throw new IllegalArgumentException("Invalid knot name");
        }
        return jdbcClient.sql("SELECT * FROM certx." + knotName)
                .query((rs, rowNum) -> new GenericKnot(
                        ((Number) rs.getObject(1)).longValue(),
                        rs.getString(2),
                        ((Number) rs.getObject(3)).longValue()
                )).list();
    }
    
}
