package io.github.thirumalx.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.thirumalx.dto.InitializerRequest;
import io.github.thirumalx.dto.InitializerResponse;
import io.github.thirumalx.service.IntializerService;

/**
 * @author Thirumal M
 */
@RestController
@RequestMapping("/initializer")
public class InitializerController {

    private final IntializerService intializerService;

    public InitializerController(IntializerService intializerService) {
        this.intializerService = intializerService;
    }

    @PostMapping("/certificates")
    public InitializerResponse insertNewCertificates(@RequestBody InitializerRequest request) {
        return intializerService.insertNewCertificates(request.path(), request.applicationId());
    }
}
