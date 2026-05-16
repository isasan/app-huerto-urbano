package com.huertoapp.service;

import com.huertoapp.dto.request.ChangePasswordRequest;
import com.huertoapp.dto.request.UpdateProfileRequest;
import com.huertoapp.dto.response.UserProfileResponse;
import com.huertoapp.exception.ResourceNotFoundException;
import com.huertoapp.model.User;
import com.huertoapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);

        testUser = User.builder()
                .id(1L)
                .username("isabel")
                .email("isabel@test.com")
                .password("encoded-password")
                .role(User.Role.USER)
                .city("Madrid")
                .countryCode("ES")
                .hemisphere(User.Hemisphere.NORTE)
                .build();
    }

    @Test
    void getProfile_returnsUserProfileResponse() {
        when(userRepository.findByUsername("isabel")).thenReturn(Optional.of(testUser));

        UserProfileResponse response = userService.getProfile("isabel");

        assertThat(response.getUsername()).isEqualTo("isabel");
        assertThat(response.getEmail()).isEqualTo("isabel@test.com");
        assertThat(response.getCity()).isEqualTo("Madrid");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    void getProfile_unknownUser_throwsResourceNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProfile_success_returnsUpdatedResponse() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setCity("Barcelona");
        request.setCountryCode("ES");

        User updatedUser = User.builder()
                .id(1L)
                .username("isabel")
                .email("isabel@test.com")
                .password("encoded-password")
                .role(User.Role.USER)
                .city("Barcelona")
                .countryCode("ES")
                .hemisphere(User.Hemisphere.NORTE)
                .build();

        when(userRepository.findByUsername("isabel")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserProfileResponse response = userService.updateProfile("isabel", request);

        assertThat(response.getCity()).isEqualTo("Barcelona");
    }

    @Test
    void changePassword_success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old-pass");
        request.setNewPassword("new-pass");

        when(userRepository.findByUsername("isabel")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("old-pass", "encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("new-pass")).thenReturn("new-encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.changePassword("isabel", request);

        verify(passwordEncoder).encode("new-pass");
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsBadCredentials() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("new-pass");

        when(userRepository.findByUsername("isabel")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("isabel", request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void deleteAccount_success() {
        when(userRepository.findByUsername("isabel")).thenReturn(Optional.of(testUser));

        userService.deleteAccount("isabel", "isabel");

        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteAccount_wrongConfirmation_throwsBadCredentials() {
        assertThatThrownBy(() -> userService.deleteAccount("isabel", "other"))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).delete(any());
    }
}
