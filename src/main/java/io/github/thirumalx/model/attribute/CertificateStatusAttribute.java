package io.github.thirumalx.model.attribute;

import java.time.Instant;

import io.github.thirumalx.model.HistorizedAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Thirumal M
 *         Knotted historized attribute: CE_STA_Certificate_Status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateStatusAttribute implements HistorizedAttribute<Long> {

    private Long id;
    private Long knotId;
    private Instant changedAt;
    private Long metadata;

    @Override
    public Long getAnchorId() {
        return id;
    }

    @Override
    public Long getValue() {
        return knotId;
    }

    @Override
    public Long getMetadataId() {
        return metadata;
    }

    @Override
    public Instant changedAt() {
        return changedAt;
    }

}
