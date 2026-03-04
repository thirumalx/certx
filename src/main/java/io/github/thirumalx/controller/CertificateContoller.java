package io.github.thirumalx.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.thirumalx.dto.Certificate;
import io.github.thirumalx.service.CertificateService;

/**
 * @author Thirumal
 */
@RestController
@RequestMapping("/application/{applicationId}/client/{clientId}/certificate")
public class CertificateContoller {

    private final CertificateService certificateService;

    public CertificateContoller(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping(value = "/")
    public ResponseEntity<Certificate> save(@RequestBody Certificate certificate) {
        certificate = certificateService.save(certificate);
        return ResponseEntity.created(URI.create("/certificate/" + certificate.getSerialNumber()))
                .body(certificate);
    }

    @GetMapping
    public ResponseEntity<List<Certificate>> listCertificates(@PathVariable Long applicationId,
            @PathVariable Long clientId) {
        List<Certificate> certificates = certificateService.listCertificates(applicationId, clientId);
        return ResponseEntity.ok(certificates);
    }

}
