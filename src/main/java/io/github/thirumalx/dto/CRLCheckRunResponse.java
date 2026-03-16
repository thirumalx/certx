package io.github.thirumalx.dto;

import java.time.Instant;

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
        int failed) {
}
