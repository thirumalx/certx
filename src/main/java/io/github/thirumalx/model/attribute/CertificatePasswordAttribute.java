package io.github.thirumalx.model.attribute;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Thirumal M
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Setter
@Getter
@Builder
@ToString
public class CertificatePasswordAttribute {

    private Long id;
    private String certificatePassword;
    private Long metadata;

}
