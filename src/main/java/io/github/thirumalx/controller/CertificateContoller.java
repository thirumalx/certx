package io.github.thirumalx.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
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
@RequestMapping("certificate")
public class CertificateContoller {
    
    private final CertificateService certificateService;

    public CertificateContoller(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping(value = "/")
    public ResponseEntity<Certificate> save(@RequestBody Certificate  certificate) {
        certificate = certificateService.save(certificate);
        return ResponseEntity.created(URI.create("/certificate/" + certificate.getSerialNumber()))
                .body(certificate);
    }

    @PostMapping(value = "/fetch-detail")
    public ResponseEntity<Certificate> getCertificateDetail(@RequestBody Certificate  certificate) {
        certificate = certificateService.getCertificateDeail(certificate);
        return ResponseEntity.ok(certificate);
    }

}
