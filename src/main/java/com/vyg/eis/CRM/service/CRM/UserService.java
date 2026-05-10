package com.vyg.eis.CRM.service.CRM;

import com.vyg.eis.CRM.common.exception.DuplicateLeadException;
import com.vyg.eis.CRM.common.exception.LastAdminException;
import com.vyg.eis.CRM.common.exception.PasswordMismatchException;
import com.vyg.eis.CRM.common.exception.ResourceNotFoundException;
import com.vyg.eis.CRM.common.exception.SelfModificationException;
import com.vyg.eis.CRM.domain.CRM.UserEntity;
import com.vyg.eis.CRM.domain.CRM.enums.UserRole;
import com.vyg.eis.CRM.dto.ChangePasswordRequest;
import com.vyg.eis.CRM.dto.UserCreateRequest;
import com.vyg.eis.CRM.dto.UserDTO;
import com.vyg.eis.CRM.dto.UserRoleChangeRequest;
import com.vyg.eis.CRM.dto.UserSummaryDTO;
import com.vyg.eis.CRM.dto.UserUpdateRequest;
import com.vyg.eis.CRM.repository.CRM.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserDTO createUser(UserCreateRequest req, UUID createdByUserId) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateLeadException("User with email " + req.getEmail() + " already exists");
        }

        UserEntity user = UserEntity.builder()
                .name(req.getName())
                .email(req.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .phone(req.getPhone())
                .designation(req.getDesignation())
                .department(req.getDepartment())
                .isActive(true)
                .isEmailVerified(false)
                .createdBy(createdByUserId)
                .updatedBy(createdByUserId)
                .build();

        user = userRepository.save(user);

        UserDTO dto = UserDTO.from(user);
        auditService.log(createdByUserId, resolveActorName(createdByUserId),
                "USER_CREATED", "USER", user.getId().toString(), null, dto, null);

        return dto;
    }

    public UserDTO updateUser(UUID userId, UserUpdateRequest req, UUID updatedByUserId) {
        UserEntity user = getActiveUserEntity(userId);

        if (req.getName() != null) user.setName(req.getName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getDesignation() != null) user.setDesignation(req.getDesignation());
        if (req.getDepartment() != null) user.setDepartment(req.getDepartment());
        if (req.getProfilePicture() != null) user.setProfilePicture(req.getProfilePicture());
        user.setUpdatedBy(updatedByUserId);

        user = userRepository.save(user);

        UserDTO dto = UserDTO.from(user);
        auditService.log(updatedByUserId, resolveActorName(updatedByUserId),
                "USER_UPDATED", "USER", userId.toString(), null, dto, null);

        return dto;
    }

    public UserDTO changeUserRole(UserRoleChangeRequest req, UUID changedByUserId) {
        if (req.getUserId().equals(changedByUserId)) {
            throw new SelfModificationException("Cannot change your own role");
        }

        UserEntity user = getActiveUserEntity(req.getUserId());
        UserRole oldRole = user.getRole();

        if (oldRole == UserRole.SUPER_ADMIN
                && userRepository.countByRoleAndIsActiveTrue(UserRole.SUPER_ADMIN) <= 1) {
            throw new LastAdminException("Cannot demote the last Super Admin");
        }

        user.setRole(req.getNewRole());
        user.setUpdatedBy(changedByUserId);
        user = userRepository.save(user);

        auditService.log(changedByUserId, resolveActorName(changedByUserId),
                "ROLE_CHANGED", "USER", user.getId().toString(),
                oldRole.name(), req.getNewRole().name() + " | reason: " + req.getReason(), null);

        return UserDTO.from(user);
    }

    public UserDTO toggleUserActive(UUID userId, UUID performedByUserId) {
        if (userId.equals(performedByUserId)) {
            throw new SelfModificationException("Cannot deactivate your own account");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (Boolean.TRUE.equals(user.getIsActive())
                && user.getRole() == UserRole.SUPER_ADMIN
                && userRepository.countByRoleAndIsActiveTrue(UserRole.SUPER_ADMIN) <= 1) {
            throw new LastAdminException("Cannot deactivate the last Super Admin");
        }

        boolean newState = !Boolean.TRUE.equals(user.getIsActive());
        user.setIsActive(newState);
        user.setUpdatedBy(performedByUserId);
        user = userRepository.save(user);

        auditService.log(performedByUserId, resolveActorName(performedByUserId),
                newState ? "USER_ACTIVATED" : "USER_DEACTIVATED",
                "USER", userId.toString(), null, null, null);

        return UserDTO.from(user);
    }

    public void deleteUser(UUID userId, UUID deletedByUserId) {
        if (userId.equals(deletedByUserId)) {
            throw new SelfModificationException("Cannot delete your own account");
        }

        UserEntity user = getActiveUserEntity(userId);

        if (user.getRole() == UserRole.SUPER_ADMIN
                && userRepository.countByRoleAndIsActiveTrue(UserRole.SUPER_ADMIN) <= 1) {
            throw new LastAdminException("Cannot delete the last Super Admin");
        }

        user.setIsActive(false);
        user.setUpdatedBy(deletedByUserId);
        userRepository.save(user);

        auditService.log(deletedByUserId, resolveActorName(deletedByUserId),
                "USER_DELETED", "USER", userId.toString(), null, null, null);
    }

    public void changePassword(UUID userId, ChangePasswordRequest req) {
        UserEntity user = getActiveUserEntity(userId);

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new PasswordMismatchException("Current password is incorrect");
        }
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        auditService.log(userId, resolveActorName(userId),
                "PASSWORD_CHANGED", "USER", userId.toString(), null, null, null);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        return UserDTO.from(getActiveUserEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(UserRole role, Boolean isActive, String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return userRepository.searchByKeyword(keyword, pageable).map(UserDTO::from);
        }
        if (role != null && isActive != null) {
            return userRepository.findByRoleAndIsActive(role, isActive, pageable).map(UserDTO::from);
        }
        if (isActive != null) {
            return userRepository.findByIsActive(isActive, pageable).map(UserDTO::from);
        }
        return userRepository.findAll(pageable).map(UserDTO::from);
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDTO> getUsersByRole(UserRole role) {
        return userRepository.findByRoleIn(List.of(role))
                .stream().map(UserSummaryDTO::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDTO> getSalesTeam() {
        return userRepository.findByRoleIn(List.of(UserRole.SALES_EXECUTIVE, UserRole.SALES_MANAGER))
                .stream().map(UserSummaryDTO::from).collect(Collectors.toList());
    }

    public void updateLastLogin(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    private UserEntity getActiveUserEntity(UUID id) {
        return userRepository.findById(id)
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private String resolveActorName(UUID actorId) {
        if (actorId == null) return "system";
        return userRepository.findById(actorId)
                .map(UserEntity::getName)
                .orElse("unknown");
    }
}
