package io.github.thirumalx.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.thirumalx.dto.CRLCheckRunResponse;
import io.github.thirumalx.scheduler.CRLCheckScheduler;
import io.github.thirumalx.scheduler.ExpiryNotificationScheduler;


/**
 * Endpoints to manually trigger scheduled jobs.
 */
@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    private final CRLCheckScheduler crlCheckScheduler;
    private final ExpiryNotificationScheduler expiryNotificationScheduler;

    public SchedulerController(CRLCheckScheduler crlCheckScheduler,
            ExpiryNotificationScheduler expiryNotificationScheduler) {
        this.crlCheckScheduler = crlCheckScheduler;
        this.expiryNotificationScheduler = expiryNotificationScheduler;
    }

    /**
     * Manually runs the CRL check and returns a summary of the run.
     */
    @PostMapping("/crl-check/run")
    public ResponseEntity<CRLCheckRunResponse> runCrlCheck() {
        return ResponseEntity.ok(crlCheckScheduler.runCrlCheckNow());
    }

    @PostMapping("/generate-report")
    public ResponseEntity<ExpiryNotificationScheduler.CertificateReportResponse> generateReport() {
        return ResponseEntity.ok(expiryNotificationScheduler.generateAndSendDailyReport());
    }

}
