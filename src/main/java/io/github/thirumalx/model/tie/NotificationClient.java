package io.github.thirumalx.model.tie;

import io.github.thirumalx.model.SimpleTie;
import lombok.Data;

/**
 * @author Thirumal M
 *         Represents the static association between a Notification and a
 *         Client.
 *         NT_sentTo_CL_receives: a Notification is sent to a Client.
 */
@Data
public class NotificationClient implements SimpleTie {

    private final Long notificationId;
    private final Long clientId;
    private final Long metadataId;

    public NotificationClient(Long notificationId, Long clientId, Long metadataId) {
        this.notificationId = notificationId;
        this.clientId = clientId;
        this.metadataId = metadataId;
    }

    @Override
    public Long getAnchor1Id() {
        return notificationId;
    }

    @Override
    public Long getAnchor2Id() {
        return clientId;
    }

    @Override
    public Long getMetadataId() {
        return metadataId;
    }

}
