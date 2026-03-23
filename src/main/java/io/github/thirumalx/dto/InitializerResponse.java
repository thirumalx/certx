package io.github.thirumalx.dto;

import java.util.List;

/**
 * DTO for initializer response containing logs and summary statistics.
 * 
 * @author Thirumal M
 */
public record InitializerResponse(
    List<String> logs,
    int totalProcessed,
    int created,
    int updated,
    int upToDate,
    int error,
    int skipped
) {
}
