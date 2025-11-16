package io.github.thirumalx.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.thirumalx.service.CertificateService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.github.thirumalx.dto.Certificate;


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

    @PostMapping("/fetch-detail")
    public ResponseEntity<Certificate> getCertificateDetail(@RequestBody Certificate  certificate) {
        certificate = certificateService.getCertificateDeail(certificate);
        return ResponseEntity.ok(certificate);
    }

}
