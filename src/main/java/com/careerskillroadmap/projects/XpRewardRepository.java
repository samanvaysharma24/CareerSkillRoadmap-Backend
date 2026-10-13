package com.careerskillroadmap.projects;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface XpRewardRepository extends JpaRepository<XpReward, Long> {

    boolean existsByOwner_IdAndEventKey(Long ownerId, String eventKey);

    List<XpReward> findAllByOwner_IdOrderByAwardedAtDesc(Long ownerId);

    long countByOwner_Id(Long ownerId);

    @org.springframework.data.jpa.repository.Query("select coalesce(sum(reward.points), 0) from XpReward reward where reward.owner.id = :ownerId")
    long sumPointsByOwnerId(@org.springframework.data.repository.query.Param("ownerId") Long ownerId);
}
