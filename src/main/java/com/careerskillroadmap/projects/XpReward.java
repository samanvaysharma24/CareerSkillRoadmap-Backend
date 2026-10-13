package com.careerskillroadmap.projects;

import java.time.Instant;

import com.careerskillroadmap.auth.UserAccount;
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
@Table(name = "xp_rewards", uniqueConstraints =
        @UniqueConstraint(name = "uk_xp_reward_owner_event", columnNames = {"user_id", "event_key"}))
public class XpReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount owner;

    @Column(name = "event_key", nullable = false, length = 150)
    private String eventKey;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false)
    private int points;

    @Column(name = "awarded_at", nullable = false)
    private Instant awardedAt = Instant.now();

    protected XpReward() {
    }

    public XpReward(UserAccount owner, String eventKey, String description, int points) {
        this.owner = owner;
        this.eventKey = eventKey;
        this.description = description;
        this.points = points;
    }

    public Long getId() {
        return id;
    }

    public String getEventKey() {
        return eventKey;
    }

    public String getDescription() {
        return description;
    }

    public int getPoints() {
        return points;
    }

    public Instant getAwardedAt() {
        return awardedAt;
    }
}
