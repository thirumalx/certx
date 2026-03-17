package io.github.thirumalx.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.github.thirumalx.model.GenericKnot;
import io.github.thirumalx.service.KnotService;
/**
 * @author Thirumal
 * To list drop down of knots and to add new knot
 */


@RestController
@RequestMapping("/knots")
public class KnotController {
    
    private final KnotService knotService;

    public KnotController(KnotService knotService) {
        this.knotService = knotService;
    }

    @GetMapping("/{knotName}")
    public List<GenericKnot> listKnots(@PathVariable String knotName) {
        return knotService.listKnots(knotName);
    }
}
