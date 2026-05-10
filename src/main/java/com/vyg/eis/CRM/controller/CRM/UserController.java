package com.vyg.eis.CRM.controller.CRM;

import com.vyg.eis.CRM.common.ApiResponse;
import com.vyg.eis.CRM.domain.CRM.AuditLog;
import com.vyg.eis.CRM.domain.CRM.enums.Permission;
import com.vyg.eis.CRM.domain.CRM.enums.UserRole;
import com.vyg.eis.CRM.dto.ChangePasswordRequest;
import com.vyg.eis.CRM.dto.UserCreateRequest;
import com.vyg.eis.CRM.dto.UserDTO;
import com.vyg.eis.CRM.dto.UserRoleChangeRequest;
import com.vyg.eis.CRM.dto.UserSummaryDTO;
import com.vyg.eis.CRM.dto.UserUpdateRequest;
import com.vyg.eis.CRM.permission.RequiresPermission;
import com.vyg.eis.CRM.repository.CRM.AuditLogRepository;
import com.vyg.eis.CRM.security.UserPrincipal;
import com.vyg.eis.CRM.service.CRM.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final AuditLogRepository auditLogRepository;

    @Operation(summary = "Get all users (paginated, filterable)")
    @GetMapping
    @RequiresPermission(Permission.USER_READ)
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserDTO> users = userService.getAllUsers(role, isActive, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(users, "Users fetched successfully"));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    @RequiresPermission(Permission.USER_READ)
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id), "User fetched successfully"));
    }

    @Operation(summary = "Get current logged-in user")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        UserPrincipal principal = requireCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(principal.getId()),
                "Current user fetched successfully"));
    }

    @Operation(summary = "Get sales team (executives + managers) for assignment dropdowns")
    @GetMapping("/sales-team")
    public ResponseEntity<ApiResponse<List<UserSummaryDTO>>> getSalesTeam() {
        requireCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(userService.getSalesTeam(), "Sales team fetched successfully"));
    }

    @Operation(summary = "Get users by role")
    @GetMapping("/by-role/{role}")
    @RequiresPermission(Permission.USER_READ)
    public ResponseEntity<ApiResponse<List<UserSummaryDTO>>> getUsersByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUsersByRole(role), "Users fetched successfully"));
    }

    @Operation(summary = "Create a new user")
    @PostMapping
    @RequiresPermission(Permission.USER_CREATE)
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserDTO user = userService.createUser(request, requireCurrentUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(user, "User created successfully"));
    }

    @Operation(summary = "Update user profile")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable UUID id,
                                                            @RequestBody UserUpdateRequest request) {
        UserPrincipal principal = requireCurrentUser();
        if (!principal.getId().equals(id)
                && !com.vyg.eis.CRM.config.RolePermissionConfig.hasPermission(
                        principal.getRole(), Permission.USER_UPDATE)) {
            throw new AccessDeniedException("You can only update your own profile");
        }
        UserDTO user = userService.updateUser(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @Operation(summary = "Change user role")
    @PatchMapping("/{id}/role")
    @RequiresPermission(Permission.USER_ROLE_CHANGE)
    public ResponseEntity<ApiResponse<UserDTO>> changeRole(@PathVariable UUID id,
                                                            @Valid @RequestBody UserRoleChangeRequest request) {
        request.setUserId(id);
        UserDTO user = userService.changeUserRole(request, requireCurrentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(user, "Role changed successfully"));
    }

    @Operation(summary = "Toggle user active status")
    @PatchMapping("/{id}/toggle-active")
    @RequiresPermission(Permission.USER_UPDATE)
    public ResponseEntity<ApiResponse<UserDTO>> toggleActive(@PathVariable UUID id) {
        UserDTO user = userService.toggleUserActive(id, requireCurrentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(user, "User status updated successfully"));
    }

    @Operation(summary = "Soft-delete a user")
    @DeleteMapping("/{id}")
    @RequiresPermission(Permission.USER_DELETE)
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id, requireCurrentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    @Operation(summary = "Change own password")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(requireCurrentUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @Operation(summary = "Get all audit logs")
    @GetMapping("/audit-logs")
    @RequiresPermission(Permission.SYSTEM_CONFIG)
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @PageableDefault(size = 20, sort = "performedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(logs, "Audit logs fetched successfully"));
    }

    @Operation(summary = "Get audit logs by user")
    @GetMapping("/audit-logs/{userId}")
    @RequiresPermission(Permission.SYSTEM_CONFIG)
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogsByUser(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "performedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByActorId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs, "Audit logs fetched successfully"));
    }

    private UserPrincipal requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        throw new AccessDeniedException("Authentication required");
    }
}
