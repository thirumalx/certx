package io.github.thirumalx.model.attribute;

import io.github.thirumalx.model.SimpleAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Thirumal M
 *         Static attribute: CE_SNO_Certificate_SerialNumber
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateSerialNumberAttribute implements SimpleAttribute<String> {

    private Long id;
    private String serialNumber;
    private Long metadata;

    @Override
    public Long getAnchorId() {
        return id;
    }

    @Override
    public String getValue() {
        return serialNumber;
    }

    @Override
    public Long getMetadataId() {
        return metadata;
    }

}
