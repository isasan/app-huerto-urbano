package com.huertoapp.service;

import com.huertoapp.dto.request.ChangePasswordRequest;
import com.huertoapp.dto.request.UpdateProfileRequest;
import com.huertoapp.dto.response.UserProfileResponse;
import com.huertoapp.exception.ResourceNotFoundException;
import com.huertoapp.model.User;
import com.huertoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getProfile(String username) {
        return toResponse(findUser(username));
    }

    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findUser(username);
        if (request.getCity() != null) {
            user.setCity(request.getCity().isBlank() ? null : request.getCity().trim());
        }
        if (request.getCountryCode() != null) {
            user.setCountryCode(request.getCountryCode().isBlank() ? null : request.getCountryCode().toUpperCase());
        }
        if (request.getHemisphere() != null) {
            user.setHemisphere(request.getHemisphere());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar().isBlank() ? null : request.getAvatar());
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = findUser(username);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("La contraseña actual no es correcta");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String authenticated, String confirmed) {
        if (!authenticated.equals(confirmed)) {
            throw new BadCredentialsException("El nombre de usuario no coincide");
        }
        User user = findUser(authenticated);
        userRepository.delete(user);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .city(user.getCity())
                .countryCode(user.getCountryCode())
                .hemisphere(user.getHemisphere())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
