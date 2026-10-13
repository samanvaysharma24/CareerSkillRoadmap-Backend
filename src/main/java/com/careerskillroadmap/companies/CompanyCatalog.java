package com.careerskillroadmap.companies;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class CompanyCatalog {

    private static final Set<String> VALID_DOMAINS = Set.of(
            "ece", "embedded", "iot", "vlsi", "robotics", "electrical", "software", "ai");

    private final List<CompanyDefinition> companies = List.of(
            new CompanyDefinition("bosch", "Bosch", List.of("embedded", "iot", "robotics", "electrical")),
            new CompanyDefinition("broadcom", "Broadcom", List.of("ece", "embedded", "iot", "vlsi")),
            new CompanyDefinition("google", "Google", List.of("software", "ai", "iot")),
            new CompanyDefinition("intel", "Intel", List.of("ece", "embedded", "vlsi", "ai")),
            new CompanyDefinition("microsoft", "Microsoft", List.of("software", "ai")),
            new CompanyDefinition("nvidia", "NVIDIA", List.of("ai", "robotics", "vlsi", "embedded")),
            new CompanyDefinition("qualcomm", "Qualcomm", List.of("ece", "embedded", "iot", "vlsi")),
            new CompanyDefinition("siemens", "Siemens", List.of("electrical", "robotics", "embedded")),
            new CompanyDefinition("tata-elxsi", "Tata Elxsi", List.of("ece", "embedded", "robotics", "software")),
            new CompanyDefinition("texas-instruments", "Texas Instruments", List.of("ece", "embedded", "electrical")),
            new CompanyDefinition("zoho", "Zoho", List.of("software", "ai"))
    );

    public List<CompanyDefinition> all() {
        return companies;
    }

    public CompanyDefinition find(String key) {
        return companies.stream().filter(company -> company.key().equals(key)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Choose a company from the available list."));
    }

    public boolean supportsDomain(String domain) {
        return VALID_DOMAINS.contains(domain);
    }
}
