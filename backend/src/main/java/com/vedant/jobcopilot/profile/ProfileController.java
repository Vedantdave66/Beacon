package com.vedant.jobcopilot.profile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final ResumeService resumeService;

    public ProfileController(ProfileService profileService, ResumeService resumeService) {
        this.profileService = profileService;
        this.resumeService = resumeService;
    }

    @GetMapping
    public ResponseEntity<ProfileView> currentProfile() {
        return profileService.findCurrent()
                .map(profile -> ResponseEntity.ok(ProfileView.from(profile, resumeService.hasResume(profile))))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public ProfileView saveProfile(@Valid @RequestBody ProfileRequest request) {
        Profile profile = profileService.save(
                request.name(),
                request.email(),
                request.targetRoles(),
                request.locations(),
                request.remotePreference(),
                request.salaryMin(),
                request.salaryMax(),
                request.seniority());
        return ProfileView.from(profile, resumeService.hasResume(profile));
    }

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ParsedResumeView uploadResume(@RequestPart("resume") MultipartFile resume) {
        return ParsedResumeView.from(resumeService.parseAndSave(resume));
    }

    public record ProfileRequest(
            @NotBlank @Size(max = 200) String name,
            @NotBlank @Email @Size(max = 320) String email,
            @NotNull @NotEmpty List<@NotBlank @Size(max = 200) String> targetRoles,
            @NotNull @NotEmpty List<@NotBlank @Size(max = 200) String> locations,
            @NotBlank @Size(max = 50) String remotePreference,
            @PositiveOrZero BigDecimal salaryMin,
            @PositiveOrZero BigDecimal salaryMax,
            @NotBlank @Size(max = 80) String seniority) {
    }

    public record ProfileView(
            UUID id,
            String name,
            String email,
            String[] targetRoles,
            String[] locations,
            String remotePreference,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String seniority,
            boolean resumeParsed) {

        static ProfileView from(Profile profile, boolean resumeParsed) {
            return new ProfileView(
                    profile.getId(),
                    profile.getName(),
                    profile.getEmail(),
                    profile.getTargetRoles(),
                    profile.getLocations(),
                    profile.getRemotePreference(),
                    profile.getSalaryMin(),
                    profile.getSalaryMax(),
                    profile.getSeniority(),
                    resumeParsed);
        }
    }

    public record ParsedResumeView(
            UUID id,
            String[] skills,
            BigDecimal yearsExperience,
            String[] techStack,
            String[] pastTitles,
            Instant updatedAt) {

        static ParsedResumeView from(ParsedResume resume) {
            return new ParsedResumeView(
                    resume.getId(),
                    resume.getSkills(),
                    resume.getYearsExperience(),
                    resume.getTechStack(),
                    resume.getPastTitles(),
                    resume.getUpdatedAt());
        }
    }
}
