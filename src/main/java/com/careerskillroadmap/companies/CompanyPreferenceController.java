package com.careerskillroadmap.companies;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preferences/companies")
public class CompanyPreferenceController {

    private final CompanyPreferenceService preferenceService;

    public CompanyPreferenceController(CompanyPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public CompanyPreferenceService.PreferencesResponse get(Authentication authentication) {
        return preferenceService.get(authentication.getName());
    }

    @PutMapping
    public CompanyPreferenceService.PreferencesResponse replace(
            @Valid @RequestBody CompanyPreferenceService.CompanySelectionRequest request,
            Authentication authentication) {
        return preferenceService.replace(authentication.getName(), request);
    }
}
