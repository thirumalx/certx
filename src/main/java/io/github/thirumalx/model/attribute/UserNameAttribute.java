package io.github.thirumalx.model.attribute;

import java.time.Instant;

import io.github.thirumalx.model.HistorizedAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Thirumal M
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNameAttribute implements HistorizedAttribute<String> {

    private Long id;
    private String name;
    private Instant changedAt;
    private Long metadata;

    @Override
    public Long getAnchorId() {
        return id;
    }

    @Override
    public String getValue() {
        return name;
    }

    @Override
    public Long getMetadataId() {
        return metadata;
    }

    @Override
    public Instant changedAt() {
        return changedAt;
    }
}
