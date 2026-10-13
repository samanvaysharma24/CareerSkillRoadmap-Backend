package com.careerskillroadmap.career;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "careers")
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String title;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String domain;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String description;

    @NotBlank
    @Size(max = 140)
    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @OneToMany(mappedBy = "career", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<Skill> skills = new ArrayList<>();

    protected Career() {
    }

    public Career(String title, String domain, String description, String slug) {
        this.title = title;
        this.domain = domain;
        this.description = description;
        this.slug = slug;
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
        skill.setCareer(this);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDomain() {
        return domain;
    }

    public String getDescription() {
        return description;
    }

    public String getSlug() {
        return slug;
    }

    public List<Skill> getSkills() {
        return skills;
    }
}
