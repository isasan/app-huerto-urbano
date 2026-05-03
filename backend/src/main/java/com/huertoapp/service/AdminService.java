package com.huertoapp.service;

import com.huertoapp.dto.request.AdminCreateUserRequest;
import com.huertoapp.dto.request.ChangeRoleRequest;
import com.huertoapp.dto.response.AdminStatsResponse;
import com.huertoapp.dto.response.AdminUserResponse;
import com.huertoapp.exception.ResourceNotFoundException;
import com.huertoapp.exception.UnauthorizedException;
import com.huertoapp.model.User;
import com.huertoapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final GardenRepository gardenRepository;
    private final CropRepository cropRepository;
    private final HarvestRepository harvestRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminUserResponse createUser(AdminCreateUserRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("El email ya está en uso");
        }
        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .role(req.role() != null ? req.role() : User.Role.USER)
                .city(req.city())
                .countryCode(req.countryCode())
                .hemisphere(req.hemisphere() != null ? req.hemisphere() : User.Hemisphere.NORTE)
                .build();
        return toResponse(userRepository.save(user));
    }

    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public AdminUserResponse getUserById(Long id) {
        return toResponse(findUser(id));
    }

    @Transactional
    public AdminUserResponse changeRole(Long id, ChangeRoleRequest request, String adminUsername) {
        requireNotSelf(id, adminUsername, "No puedes cambiar tu propio rol");
        User user = findUser(id);
        user.setRole(User.Role.valueOf(request.getRole()));
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id, String adminUsername) {
        requireNotSelf(id, adminUsername, "No puedes eliminarte a ti mismo");
        userRepository.delete(findUser(id));
    }

    public AdminStatsResponse getStats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalGardens(gardenRepository.count())
                .totalCrops(cropRepository.count())
                .totalHarvests(harvestRepository.count())
                .totalTasks(taskRepository.count())
                .build();
    }

    private void requireNotSelf(Long targetId, String adminUsername, String message) {
        userRepository.findByUsername(adminUsername)
                .filter(u -> u.getId().equals(targetId))
                .ifPresent(u -> { throw new UnauthorizedException(message); });
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    private AdminUserResponse toResponse(User u) {
        return AdminUserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .role(u.getRole().name())
                .city(u.getCity())
                .countryCode(u.getCountryCode())
                .hemisphere(u.getHemisphere() != null ? u.getHemisphere().name() : null)
                .createdAt(u.getCreatedAt())
                .build();
    }
}
