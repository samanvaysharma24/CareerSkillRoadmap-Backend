package com.careerskillroadmap.career;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRepository extends JpaRepository<Career, Long> {

    Optional<Career> findBySlug(String slug);
}
