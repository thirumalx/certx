package io.github.thirumalx.model;

/**
 * @author Thirumal
 *         A knot represents a finite set of values used to describe states of
 *         entities or relationships.
 */
public sealed interface Knot<T> permits SimpleKnot, HistorizedKnot {

    public static final Long DELETED = 0L;
    public static final Long ACTIVE = 1L;
    public static final Long REVOKED = 2L;

    Long getId();

    String description();

    Long getMetadata();
}