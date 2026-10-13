package com.careerskillroadmap.assessment;

import java.util.ArrayList;
import java.util.List;

import com.careerskillroadmap.career.Career;
import com.careerskillroadmap.auth.UserAccount;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessments")
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "assessment_domains", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "domain", nullable = false, length = 40)
    private List<String> domains = new ArrayList<>();

    @Column(nullable = false, length = 1000)
    private String interests;

    @Column(nullable = false, length = 300)
    private String customInterest;

    @Column(nullable = false, length = 20)
    private String skillLevel;

    @ElementCollection
    @CollectionTable(name = "assessment_goals", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "goal", nullable = false, length = 40)
    private List<String> goals = new ArrayList<>();

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<AssessmentRecommendation> recommendations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_career_id")
    private Career roadmapCareer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount owner;

    protected Assessment() {
    }

    public Assessment(List<String> domains, String interests, String customInterest, String skillLevel, List<String> goals) {
        this.domains = new ArrayList<>(domains);
        this.interests = interests;
        this.customInterest = customInterest;
        this.skillLevel = skillLevel;
        this.goals = new ArrayList<>(goals);
    }

    public void addRecommendation(AssessmentRecommendation recommendation) {
        recommendations.add(recommendation);
        recommendation.setAssessment(this);
    }

    public void clearRecommendations() {
        recommendations.clear();
    }

    public void setRoadmapCareer(Career roadmapCareer) {
        this.roadmapCareer = roadmapCareer;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public List<String> getDomains() {
        return domains;
    }

    public String getInterests() {
        return interests;
    }

    public String getCustomInterest() {
        return customInterest;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public List<String> getGoals() {
        return goals;
    }

    public List<AssessmentRecommendation> getRecommendations() {
        return recommendations;
    }

    public Career getRoadmapCareer() {
        return roadmapCareer;
    }
}
