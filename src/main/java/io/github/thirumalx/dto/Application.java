package io.github.thirumalx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Thirumal
 */
@Data
@AllArgsConstructor
@Builder
public class Application {

    private Long id;
    private String applicationName;
    private String uniqueId;
}
