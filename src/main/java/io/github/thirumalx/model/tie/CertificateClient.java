package io.github.thirumalx.model.tie;

import io.github.thirumalx.model.SimpleTie;
import lombok.Data;

/**
 * @author Thirumal M
 *         Represents the static association between a Certificate and its
 *         Client owner.
 *         CE_belongsTo_CL_owns: a Certificate belongs to a Client.
 */
@Data
public class CertificateClient implements SimpleTie {

    private final Long certificateId;
    private final Long clientId;
    private final Long metadataId;

    public CertificateClient(Long certificateId, Long clientId, Long metadataId) {
        this.certificateId = certificateId;
        this.clientId = clientId;
        this.metadataId = metadataId;
    }

    @Override
    public Long getAnchor1Id() {
        return certificateId;
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
