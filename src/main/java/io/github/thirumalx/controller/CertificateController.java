package io.github.thirumalx.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.github.thirumalx.dto.Certificate;
import io.github.thirumalx.dto.PageRequest;
import io.github.thirumalx.dto.PageResponse;
import io.github.thirumalx.service.CertificateService;
import jakarta.validation.Valid;

/**
 * @author Thirumal
 */
@RestController
@RequestMapping("/application/{applicationId}/client/{clientId}/certificate")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping("/")
    public ResponseEntity<Certificate> save(@PathVariable Long applicationId,
            @PathVariable Long clientId, @Valid @RequestBody Certificate certificate) {
        Certificate saved = certificateService.save(applicationId, clientId, certificate);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Certificate> getById(@PathVariable Long applicationId, @PathVariable Long clientId,
            @PathVariable Long id) {
        return ResponseEntity.ok(certificateService.getCertificate(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long applicationId, @PathVariable Long clientId,
            @PathVariable Long id) {
        certificateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<Certificate>> listCertificates(@PathVariable Long applicationId,
            @PathVariable Long clientId, @RequestParam(required = false) String status,
            @Valid PageRequest pageRequest) {
        PageResponse<Certificate> certificates = certificateService.listCertificates(applicationId, clientId,
                status, pageRequest);
        return ResponseEntity.ok(certificates);
    }

    @PostMapping("/validate")
    public ResponseEntity<Certificate> validateCertificate(@PathVariable Long applicationId,
            @PathVariable Long clientId,
            @RequestParam String path,
            @RequestParam(required = false) String password) {
        return ResponseEntity.ok(certificateService.validateCertificate(path, password));
    }

}
