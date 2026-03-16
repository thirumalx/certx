package io.github.thirumalx.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.thirumalx.dto.CRLCheckRunResponse;
import io.github.thirumalx.scheduler.CRLCheckScheduler;

/**
 * Endpoints to manually trigger scheduled jobs.
 */
@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    private final CRLCheckScheduler crlCheckScheduler;

    public SchedulerController(CRLCheckScheduler crlCheckScheduler) {
        this.crlCheckScheduler = crlCheckScheduler;
    }

    /**
     * Manually runs the CRL check and returns a summary of the run.
     */
    @PostMapping("/crl-check/run")
    public ResponseEntity<CRLCheckRunResponse> runCrlCheck() {
        return ResponseEntity.ok(crlCheckScheduler.runCrlCheckNow());
    }
}
