package com.vedant.jobcopilot.profile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Profile> findCurrent() {
        return profileRepository.findFirstByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Profile requireCurrent() {
        return findCurrent().orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Create a profile before uploading a resume"));
    }

    @Transactional
    public Profile save(
            String name,
            String email,
            List<String> targetRoles,
            List<String> locations,
            String remotePreference,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String seniority) {
        if (salaryMin != null && salaryMax != null && salaryMax.compareTo(salaryMin) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum salary must be at least the minimum");
        }

        String[] roles = clean(targetRoles);
        String[] cleanLocations = clean(locations);
        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> new Profile(
                        name.trim(), email.trim(), roles, cleanLocations, remotePreference,
                        salaryMin, salaryMax, seniority));

        profile.update(
                name.trim(), email.trim(), roles, cleanLocations, remotePreference,
                salaryMin, salaryMax, seniority);
        return profileRepository.save(profile);
    }

    private String[] clean(List<String> values) {
        return values.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toArray(String[]::new);
    }
}
