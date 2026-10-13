package com.careerskillroadmap.companies;

import com.careerskillroadmap.auth.UserAccount;
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
@Table(name = "company_preferences", uniqueConstraints =
        @UniqueConstraint(name = "uk_company_preference_owner_key", columnNames = {"user_id", "company_key"}))
public class CompanyPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount owner;

    @jakarta.persistence.Column(name = "company_key", nullable = false, length = 80)
    private String companyKey;

    protected CompanyPreference() {
    }

    public CompanyPreference(UserAccount owner, String companyKey) {
        this.owner = owner;
        this.companyKey = companyKey;
    }

    public String getCompanyKey() {
        return companyKey;
    }
}
