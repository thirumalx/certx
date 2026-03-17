package io.github.thirumalx.model;

/**
 * Generic knot representation suitable for querying arbitrary knot tables.
 */
public record GenericKnot(Long id, String value, Long metadata) implements SimpleKnot<String> {

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String description() {
        return value;
    }

    @Override
    public Long getMetadata() {
        return metadata;
    }
}
