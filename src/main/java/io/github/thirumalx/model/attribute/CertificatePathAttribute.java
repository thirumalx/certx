package io.github.thirumalx.model.attribute;

import io.github.thirumalx.model.SimpleAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Thirumal M
 *         Static attribute: CE_CEP_Certificate_CertificatePath
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificatePathAttribute implements SimpleAttribute<String> {

    private Long id;
    private String certificatePath;
    private Long metadata;

    @Override
    public Long getAnchorId() {
        return id;
    }

    @Override
    public String getValue() {
        return certificatePath;
    }

    @Override
    public Long getMetadataId() {
        return metadata;
    }

}
