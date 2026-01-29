package io.github.thirumalx.model.attribute;

import java.time.Instant;

import io.github.thirumalx.model.HistorizedAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Thirumal
 */
@Data
@Builder
@AllArgsConstructor
public class ApplicationUniqueIdAttribute implements HistorizedAttribute<String> {

    private Long apUidApId;
    private String apUidApplicationUniqueId;
    private Long metadataApUid;
    private Instant changedAt;

    @Override
    public Long getAnchorId() {
        return apUidApId;
    }

    @Override
    public String getValue() {
        return apUidApplicationUniqueId;
    }

    @Override
    public Long getMetadataId() {
        return metadataApUid;
    }

    @Override
    public Instant changedAt() {
        return changedAt;
    }

}
