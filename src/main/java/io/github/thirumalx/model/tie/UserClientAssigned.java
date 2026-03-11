package io.github.thirumalx.model.tie;

import java.time.Instant;

import io.github.thirumalx.model.HistorizedTie;
import lombok.Data;

/**
 * @author Thirumal M
 * Represents the historized assignment of a Client to a User.
 */
@Data
public class UserClientAssigned implements HistorizedTie {

    private final Long userId;
    private final Long clientId;
    private final Long metadataId;
    private final Instant changedAt;

    public UserClientAssigned(Long userId, Long clientId, Long metadataId, Instant changedAt) {
        this.userId = userId;
        this.clientId = clientId;
        this.metadataId = metadataId;
        this.changedAt = changedAt;
    }

    @Override
    public Long getAnchor1Id() {
        return userId;
    }

    @Override
    public Long getAnchor2Id() {
        return clientId;
    }

    @Override
    public Long getMetadataId() {
        return metadataId;
    }

    @Override
    public Instant changedAt() {
        return changedAt;
    }
}
