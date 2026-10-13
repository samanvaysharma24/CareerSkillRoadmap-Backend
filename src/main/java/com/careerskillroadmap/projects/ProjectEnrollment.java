package com.careerskillroadmap.projects;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.careerskillroadmap.auth.UserAccount;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "project_enrollments", uniqueConstraints =
        @UniqueConstraint(name = "uk_project_enrollment_owner_key", columnNames = {"user_id", "project_key"}))
public class ProjectEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount owner;

    @Column(name = "project_key", nullable = false, length = 80)
    private String projectKey;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @ElementCollection
    @CollectionTable(name = "project_completed_milestones",
            joinColumns = @JoinColumn(name = "enrollment_id", nullable = false),
            uniqueConstraints = @UniqueConstraint(columnNames = {"enrollment_id", "milestone_key"}))
    @Column(name = "milestone_key", nullable = false, length = 40)
    private Set<String> completedMilestones = new HashSet<>();

    protected ProjectEnrollment() {
    }

    public ProjectEnrollment(UserAccount owner, String projectKey) {
        this.owner = owner;
        this.projectKey = projectKey;
        this.startedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Set<String> getCompletedMilestones() {
        return completedMilestones;
    }

    public void completeMilestone(String milestoneKey, boolean projectComplete) {
        completedMilestones.add(milestoneKey);
        if (projectComplete && completedAt == null) completedAt = Instant.now();
    }
}
