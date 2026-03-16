package io.github.thirumalx.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.thirumalx.dto.CertificateValidityResponse;
import io.github.thirumalx.service.CertificateService;

/**
 * API to validate certificates by serial number.
 */
@RestController
@RequestMapping("/certificate")
public class CertificateValidityController {

    private final CertificateService certificateService;

    public CertificateValidityController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/validity")
    public ResponseEntity<CertificateValidityResponse> checkValidity(
            @RequestParam String serialNumber,
            @RequestParam(required = false) String applicationUniqueId,
            @RequestParam(required = false) String clientUniqueId) {
        return ResponseEntity.ok(
                certificateService.checkCertificateValidity(serialNumber, applicationUniqueId, clientUniqueId));
    }
}
