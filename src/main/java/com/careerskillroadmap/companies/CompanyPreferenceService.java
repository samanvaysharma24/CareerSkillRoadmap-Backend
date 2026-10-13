package com.careerskillroadmap.companies;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.careerskillroadmap.auth.UserAccount;
import com.careerskillroadmap.auth.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyPreferenceService {

    private final CompanyCatalog catalog;
    private final CompanyPreferenceRepository preferenceRepository;
    private final UserAccountRepository userRepository;
    private final CompanyCatalog companyCatalog;

    public CompanyPreferenceService(
            CompanyCatalog catalog,
            CompanyPreferenceRepository preferenceRepository,
            UserAccountRepository userRepository) {
        this.catalog = catalog;
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
        this.companyCatalog = catalog;
    }

    @Transactional(readOnly = true)
    public PreferencesResponse get(String email) {
        UserAccount owner = user(email);
        Set<String> selected = preferenceRepository.findAllByOwner_Id(owner.getId()).stream()
                .map(CompanyPreference::getCompanyKey)
                .collect(Collectors.toSet());
        return new PreferencesResponse(catalog.all(), selected.stream().sorted().toList(),
                "Companies are examples only; listing is not an endorsement or a guarantee of employment.");
    }

    public PreferencesResponse replace(String email, CompanySelectionRequest request) {
        UserAccount owner = user(email);
        List<String> keys = request.companyKeys().stream().distinct().toList();
        keys.forEach(catalog::find);
        preferenceRepository.deleteAllByOwner_Id(owner.getId());
        preferenceRepository.saveAll(keys.stream().map(key -> new CompanyPreference(owner, key)).toList());
        return get(email);
    }

    @Transactional(readOnly = true)
    public Set<String> preferredDomains(String email) {
        return preferenceRepository.findAllByOwner_Id(user(email).getId()).stream()
                .map(CompanyPreference::getCompanyKey)
                .map(key -> companyCatalog.find(key).domains())
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    private UserAccount user(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated account not found."));
    }

    public record CompanySelectionRequest(
            @jakarta.validation.constraints.Size(max = 30) List<@jakarta.validation.constraints.NotBlank String> companyKeys) {
        public CompanySelectionRequest {
            companyKeys = companyKeys == null ? List.of() : List.copyOf(companyKeys);
        }
    }

    public record PreferencesResponse(List<CompanyDefinition> companies, List<String> selectedCompanyKeys,
                                      String notice) {
    }
}
