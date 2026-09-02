package com.vedant.jobcopilot.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.vedant.jobcopilot.job.JobController.JobView;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<ApplicationView> listApplications() {
        return applicationService.listApplications().stream().map(ApplicationView::from).toList();
    }

    @PostMapping("/jobs/{jobId}")
    public ApplicationView saveJob(@PathVariable UUID jobId) {
        return ApplicationView.from(applicationService.saveJob(jobId));
    }

    @PatchMapping("/{applicationId}/status")
    public ApplicationView updateStatus(
            @PathVariable UUID applicationId,
            @Valid @RequestBody StatusRequest request) {
        return ApplicationView.from(applicationService.updateStatus(applicationId, request.status()));
    }

    public record StatusRequest(@NotBlank String status) {
    }

    public record ApplicationView(
            UUID id,
            String status,
            Instant appliedAt,
            String notes,
            String coverLetterText,
            String resumeVersion,
            JobView job) {

        public static ApplicationView from(JobApplication application) {
            return new ApplicationView(
                    application.getId(),
                    application.getStatus().value(),
                    application.getAppliedAt(),
                    application.getNotes(),
                    application.getCoverLetterText(),
                    application.getResumeVersion(),
                    JobView.from(application.getJob()));
        }
    }
}
