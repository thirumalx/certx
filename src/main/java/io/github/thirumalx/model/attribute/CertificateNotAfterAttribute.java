package io.github.thirumalx.model.attribute;

import java.time.Instant;

import io.github.thirumalx.model.SimpleAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Thirumal M
 *         Static attribute: CE_NAF_Certificate_NotAfter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateNotAfterAttribute implements SimpleAttribute<Instant> {

    private Long id;
    private Instant notAfter;
    private Long metadata;

    @Override
    public Long getAnchorId() {
        return id;
    }

    @Override
    public Instant getValue() {
        return notAfter;
    }

    @Override
    public Long getMetadataId() {
        return metadata;
    }

}
