package io.github.thirumalx.model.attribute;

import io.github.thirumalx.model.SimpleAttribute;
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
public class ClientUniqueIdAttribute implements SimpleAttribute<String> {

    private Long id;
    private String uniqueId;
    private Long metadata;

    @Override
    public Long getAnchorId() {
        return id;
    }

    @Override
    public String getValue() {
        return uniqueId;
    }

    @Override
    public Long getMetadataId() {
        return metadata;
    }
}
