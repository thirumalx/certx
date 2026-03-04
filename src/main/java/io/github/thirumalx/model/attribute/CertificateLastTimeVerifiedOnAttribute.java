package io.github.thirumalx.model.attribute;

import java.time.Instant;

import io.github.thirumalx.model.SimpleAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Thirumal M
 *         Static attribute: CE_LTV_Certificate_LastTimeVerifiedOn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateLastTimeVerifiedOnAttribute implements SimpleAttribute<Instant> {

    private Long id;
    private Instant lastTimeVerifiedOn;
    private Long metadata;

    @Override
    public Long getAnchorId() {
        return id;
    }

    @Override
    public Instant getValue() {
        return lastTimeVerifiedOn;
    }

    @Override
    public Long getMetadataId() {
        return metadata;
    }

}
