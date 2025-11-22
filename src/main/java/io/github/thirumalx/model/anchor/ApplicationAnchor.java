package io.github.thirumalx.model.anchor;

import io.github.thirumalx.model.Anchor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApplicationAnchor implements  Anchor {
    
    private Long apId;
    private Long metadataAp;

    @Override
    public Long getId() {
        return apId;
    }

    @Override
    public Long getMetadata() {
        return metadataAp;
    }

}
