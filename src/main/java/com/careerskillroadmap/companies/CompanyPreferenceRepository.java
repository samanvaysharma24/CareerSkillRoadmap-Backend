package com.careerskillroadmap.companies;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyPreferenceRepository extends JpaRepository<CompanyPreference, Long> {

    List<CompanyPreference> findAllByOwner_Id(Long ownerId);

    void deleteAllByOwner_Id(Long ownerId);
}
