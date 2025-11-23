package io.github.thirumalx.model;

import java.time.Instant;
/**
 * @author Thirumal M
 * For attributes that maintain historical changes with timestamps.
 */
public non-sealed interface HistorizedAttribute<T> extends Attribute<T> {

     Instant changedAt();

}
