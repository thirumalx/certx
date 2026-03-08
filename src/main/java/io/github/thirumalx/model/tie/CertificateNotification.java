package io.github.thirumalx.model.tie;

import io.github.thirumalx.model.SimpleTie;
import lombok.Data;

/**
 * @author Thirumal M
 *         Represents the static association between a Certificate and a
 *         Notification.
 *         CE_isNotifiedBy_NT_notifies: a Certificate is notified by a
 *         Notification.
 */
@Data
public class CertificateNotification implements SimpleTie {

    private final Long certificateId;
    private final Long notificationId;
    private final Long metadataId;

    public CertificateNotification(Long certificateId, Long notificationId, Long metadataId) {
        this.certificateId = certificateId;
        this.notificationId = notificationId;
        this.metadataId = metadataId;
    }

    @Override
    public Long getAnchor1Id() {
        return certificateId;
    }

    @Override
    public Long getAnchor2Id() {
        return notificationId;
    }

    @Override
    public Long getMetadataId() {
        return metadataId;
    }

}
