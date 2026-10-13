package com.careerskillroadmap.career;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CareerService {

    private final CareerRepository careerRepository;

    public CareerService(CareerRepository careerRepository) {
        this.careerRepository = careerRepository;
    }

    public List<CareerResponse> findAll() {
        return careerRepository.findAll().stream()
                .map(CareerResponse::from)
                .toList();
    }

    public CareerResponse findById(Long id) {
        Career career = careerRepository.findById(id)
                .orElseThrow(() -> new CareerNotFoundException(id));
        return CareerResponse.from(career);
    }
}
