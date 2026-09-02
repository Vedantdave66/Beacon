package com.vedant.jobcopilot.profile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResumeService {

    private static final String PARSE_PROMPT = """
            Extract the resume into this structure. Keep skills and technologies concise, do not invent details,
            and use null for yearsExperience when it cannot be estimated. Return only the requested structured data.
            """;

    private final ProfileService profileService;
    private final ParsedResumeRepository resumeRepository;
    private final ChatClient chatClient;

    public ResumeService(
            ProfileService profileService,
            ParsedResumeRepository resumeRepository,
            ChatClient.Builder chatClientBuilder) {
        this.profileService = profileService;
        this.resumeRepository = resumeRepository;
        this.chatClient = chatClientBuilder.build();
    }

    public boolean hasResume(Profile profile) {
        return resumeRepository.existsByProfileId(profile.getId());
    }

    public ParsedResume parseAndSave(MultipartFile file) {
        Profile profile = profileService.requireCurrent();
        String rawText = extractText(file);

        ResumeDetails details;
        try {
            details = chatClient.prompt()
                    .system(PARSE_PROMPT)
                    .user("Resume text:\n\n" + rawText)
                    .call()
                    .entity(ResumeDetails.class);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Groq could not parse the resume. Check the API key and model, then try again.",
                    exception);
        }

        if (details == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Groq returned an empty resume result");
        }

        ParsedResume resume = resumeRepository.findByProfileId(profile.getId())
                .orElseGet(() -> new ParsedResume(profile));
        resume.update(
                rawText,
                toArray(details.skills()),
                details.yearsExperience(),
                toArray(details.techStack()),
                toArray(details.pastTitles()));
        return resumeRepository.save(resume);
    }

    private String extractText(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null
                || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a PDF resume to upload");
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(document).trim();
            if (text.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No readable text was found in that PDF");
            }
            return text;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded PDF could not be read", exception);
        }
    }

    private String[] toArray(List<String> values) {
        if (values == null) {
            return new String[0];
        }
        return values.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toArray(String[]::new);
    }

    public record ResumeDetails(
            List<String> skills,
            BigDecimal yearsExperience,
            List<String> techStack,
            List<String> pastTitles) {
    }
}
