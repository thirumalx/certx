package io.github.thirumalx.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/certificates/{path}")
    public List<String> insertNewCertificates(@PathVariable String path) {
        return intializerService.insertNewCertificates(path);
    }
}
