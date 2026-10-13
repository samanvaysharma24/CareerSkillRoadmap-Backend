package com.careerskillroadmap.career;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/careers")
public class CareerController {

    private final CareerService careerService;

    public CareerController(CareerService careerService) {
        this.careerService = careerService;
    }

    @GetMapping
    public List<CareerResponse> findAll() {
        return careerService.findAll();
    }

    @GetMapping("/{id}")
    public CareerResponse findById(@PathVariable Long id) {
        return careerService.findById(id);
    }
}
