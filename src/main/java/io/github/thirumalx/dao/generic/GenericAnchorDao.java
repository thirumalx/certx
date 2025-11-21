/**
 * 
 */
package io.github.thirumalx.dao.generic;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import io.github.thirumalx.model.Anchor;

/**
 * @author Thirumal M
 * Anchor tables usually contain only the anchor ID (auto-generated PK) and optionally other metadata.
 */
public abstract class GenericAnchorDao<T extends Anchor> {

	private final JdbcClient jdbc;
	private final String tableName;
	private final Class<T> type;
	private final String idColumn;

	protected GenericAnchorDao(JdbcClient jdbc, String tableName, String idColumn, Class<T> type) {
		this.jdbc = jdbc;
		this.tableName = tableName;
		this.idColumn = idColumn;
		this.type = type;
	}

	public Long insert() {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.sql("INSERT INTO " + tableName + " DEFAULT VALUES").update(keyHolder);
		Number key = keyHolder.getKey();
		return key != null ? key.longValue() : null;
	}

	public Optional<T> findById(Long id) {
		return jdbc.sql("SELECT * FROM " + tableName + " WHERE " + idColumn + " = :id").param("id", id).query(type)
				.optional();
	}

}
