package io.github.thirumalx.model.attribute;

import io.github.thirumalx.model.SimpleAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Thirumal
 */
@Data
@Builder
@AllArgsConstructor
public class ApplicationUniqueIdAttribute implements SimpleAttribute<String> {

    private Long apUidApId;
    private String apUidApplicationUniqueId;
    private Long metadataApUid;

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

}
