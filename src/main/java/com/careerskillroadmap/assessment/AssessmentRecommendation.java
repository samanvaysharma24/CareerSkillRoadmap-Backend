package com.careerskillroadmap.assessment;

import com.careerskillroadmap.career.Career;
import jakarta.persistence.Column;
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
@Table(name = "assessment_recommendations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_assessment_recommendation_career",
                columnNames = {"assessment_id", "career_id"})
})
public class AssessmentRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "career_id", nullable = false)
    private Career career;

    @Column(nullable = false)
    private int matchScore;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false)
    private int position;

    protected AssessmentRecommendation() {
    }

    public AssessmentRecommendation(Career career, int matchScore, String reason, int position) {
        this.career = career;
        this.matchScore = matchScore;
        this.reason = reason;
        this.position = position;
    }

    void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public Career getCareer() {
        return career;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public String getReason() {
        return reason;
    }

    public int getPosition() {
        return position;
    }
}
