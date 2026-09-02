package com.vedant.jobcopilot.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ProfileServiceTests {

    private final ProfileRepository repository = mock(ProfileRepository.class);
    private final ProfileService service = new ProfileService(repository);

    @Test
    void savesACleanProfile() {
        when(repository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(repository.save(any(Profile.class))).thenAnswer(call -> call.getArgument(0));

        Profile profile = service.save(
                " Vedant ",
                " vedant@example.com ",
                List.of("Java Developer", " Java Developer ", "Backend Developer"),
                List.of("Toronto", " Remote "),
                "Remote",
                new BigDecimal("80000"),
                new BigDecimal("120000"),
                "Mid-level");

        assertThat(profile.getName()).isEqualTo("Vedant");
        assertThat(profile.getEmail()).isEqualTo("vedant@example.com");
        assertThat(profile.getTargetRoles()).containsExactly("Java Developer", "Backend Developer");
        assertThat(profile.getLocations()).containsExactly("Toronto", "Remote");
    }

    @Test
    void rejectsAnInvertedSalaryRange() {
        assertThatThrownBy(() -> service.save(
                "Vedant",
                "vedant@example.com",
                List.of("Java Developer"),
                List.of("Toronto"),
                "Remote",
                new BigDecimal("120000"),
                new BigDecimal("80000"),
                "Mid-level"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Maximum salary");
    }
}
