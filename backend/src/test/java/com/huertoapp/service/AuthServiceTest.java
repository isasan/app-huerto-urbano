package com.huertoapp.service;

import com.huertoapp.dto.request.LoginRequest;
import com.huertoapp.dto.request.RegisterRequest;
import com.huertoapp.dto.response.AuthResponse;
import com.huertoapp.model.User;
import com.huertoapp.repository.UserRepository;
import com.huertoapp.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtTokenProvider);
    }

    @Test
    void register_success_returnsAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("isabel");
        request.setEmail("isabel@test.com");
        request.setPassword("secret123");

        User savedUser = User.builder()
                .username("isabel")
                .email("isabel@test.com")
                .password("encoded")
                .role(User.Role.USER)
                .build();

        when(userRepository.existsByUsername("isabel")).thenReturn(false);
        when(userRepository.existsByEmail("isabel@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateTokenFromUsername("isabel")).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpiration()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("isabel");
        assertThat(response.getEmail()).isEqualTo("isabel@test.com");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    void register_duplicateUsername_throwsIllegalArgument() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("isabel");
        request.setEmail("isabel@test.com");
        request.setPassword("secret123");

        when(userRepository.existsByUsername("isabel")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("usuario ya existe");
    }

    @Test
    void register_duplicateEmail_throwsIllegalArgument() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("isabel");
        request.setEmail("isabel@test.com");
        request.setPassword("secret123");

        when(userRepository.existsByUsername("isabel")).thenReturn(false);
        when(userRepository.existsByEmail("isabel@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
    }

    @Test
    void login_success_returnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("isabel");
        request.setPassword("secret123");

        Authentication auth = new UsernamePasswordAuthenticationToken("isabel", "secret123");
        User user = User.builder()
                .username("isabel")
                .email("isabel@test.com")
                .role(User.Role.USER)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("jwt-token");
        when(userRepository.findByUsername("isabel")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.getExpiration()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("isabel");
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("isabel");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
