package io.github.thirumalx.dto;

import java.time.Instant;
import java.util.List;

/**
 * Summary of a CRL check run.
 */
public record CRLCheckRunResponse(
        Instant startedAt,
        Instant finishedAt,
        int totalActive,
        int processed,
        int revoked,
        int skipped,
        int failed,
        List<CertificateLog> logs) {

    public record CertificateLog(String serialNumber, String status, String message) {
    }
}
