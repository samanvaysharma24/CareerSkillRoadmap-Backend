package com.careerskillroadmap.assessment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    java.util.Optional<Assessment> findByIdAndOwner_EmailIgnoreCase(Long id, String email);

    java.util.Optional<Assessment> findFirstByOwner_IdOrderByIdDesc(Long ownerId);
}
