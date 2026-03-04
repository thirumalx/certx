package io.github.thirumalx.model.anchor;

import io.github.thirumalx.model.Anchor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Thirumal M
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateAnchor implements Anchor {

    private Long id;
    private Long metadata;

}
