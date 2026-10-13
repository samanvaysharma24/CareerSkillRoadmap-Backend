package com.careerskillroadmap.projects;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectEnrollmentRepository extends JpaRepository<ProjectEnrollment, Long> {

    List<ProjectEnrollment> findAllByOwner_Id(Long ownerId);

    Optional<ProjectEnrollment> findByOwner_IdAndProjectKey(Long ownerId, String projectKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select enrollment from ProjectEnrollment enrollment where enrollment.owner.id = :ownerId and enrollment.projectKey = :projectKey")
    Optional<ProjectEnrollment> findForUpdate(@Param("ownerId") Long ownerId, @Param("projectKey") String projectKey);
}
