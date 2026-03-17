package io.github.thirumalx.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import io.github.thirumalx.model.Knot;

/**
 * @author Thirumal
 *         Knot tables usually contain the knot ID (auto-generated PK), knot
 *         value, and optionally other metadata.
 *         Used for managing static reference data shared across multiple
 *         entities.
 *         Example: Status, Category, Type, etc.
 */
public abstract class KnotDao<T extends Knot<Long>> {

	private final JdbcClient jdbc;
	private final String tableName;
	private final String idColumn;
	private final String metadataColumn;

	protected KnotDao(JdbcClient jdbc, String tableName, String idColumn, String metadataColumn) {
		this.jdbc = jdbc;
		this.tableName = tableName;
		this.idColumn = idColumn;
		this.metadataColumn = metadataColumn;
	}

	public Long insert(Long metadata) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.sql("INSERT INTO " + tableName + " (" + metadataColumn + ") VALUES (:metadata) RETURNING " + idColumn)
				.param("metadata", metadata)
				.update(keyHolder);

		Number key = keyHolder.getKey();
		return key != null ? key.longValue() : null;
	}

	public Optional<T> findById(Long id) {
		return jdbc.sql("SELECT * FROM " + tableName + " WHERE " + idColumn + " = :id").param("id", id)
				.query(rowMapper())
				.optional();
	}
	/**
	 * Here knot name is same as table name
	 */
	public List<T> findAll() {
		return jdbc.sql("SELECT * FROM certx." + tableName).query(rowMapper()).list();
	}

	protected abstract RowMapper<T> rowMapper();
}
