package io.github.thirumalx.dto;

import java.time.LocalDateTime;

public record CertificateValidityResponse(
        String serialNumber,
        boolean valid,
        String reason,
        LocalDateTime notAfter,
        LocalDateTime revokedOn) {
}
