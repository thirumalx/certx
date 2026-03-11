package io.github.thirumalx.model.anchor;

import io.github.thirumalx.model.Anchor;
import lombok.Data;

/**
 * @author Thirumal M
 */
@Data
public class UserAnchor implements Anchor {

    private final Long id;
    private final Long metadata;

    public UserAnchor(Long id, Long metadata) {
        this.id = id;
        this.metadata = metadata;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getMetadata() {
        return metadata;
    }
}
