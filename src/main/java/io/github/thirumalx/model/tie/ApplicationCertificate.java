package io.github.thirumalx.model.tie;

import io.github.thirumalx.model.SimpleTie;
import lombok.Data;

/**
 * @author Thirumal M
 * Represents the static association between an Application and a Certificate.
 * ap_uses_ce_isusedby: an Application uses a Certificate.
 */
@Data
public class ApplicationCertificate implements SimpleTie {

    private final Long applicationId;
    private final Long certificateId;
    private final Long metadataId;

    public ApplicationCertificate(Long applicationId, Long certificateId, Long metadataId) {
        this.applicationId = applicationId;
        this.certificateId = certificateId;
        this.metadataId = metadataId;
    }

    @Override
    public Long getAnchor1Id() {
        return applicationId;
    }

    @Override
    public Long getAnchor2Id() {
        return certificateId;
    }

    @Override
    public Long getMetadataId() {
        return metadataId;
    }
}
