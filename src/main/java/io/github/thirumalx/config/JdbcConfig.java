/**
 * 
 */
package io.github.thirumalx.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * @author Thirumal
 */
@Configuration
public class JdbcConfig {

	@Bean
	JdbcClient jdbcClient(DataSource dataSource) {
		return JdbcClient.create(dataSource);
	}
	
}
