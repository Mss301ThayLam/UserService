package com.mss.user_service.controller;

import com.mss.user_service.dto.AddressDto;
import com.mss.user_service.dto.UserPreferencesDto;
import com.mss.user_service.entity.UserProfile;
import com.mss.user_service.mapper.UserProfileMapper;
import com.mss.user_service.payloads.requests.AddLoyaltyPointsRequest;
import com.mss.user_service.payloads.requests.AdminCreateUserRequest;
import com.mss.user_service.payloads.requests.CompleteProfileRequest;
import com.mss.user_service.payloads.requests.UpdateProfileRequest;
import com.mss.user_service.payloads.response.BaseResponse;
import com.mss.user_service.payloads.response.LoyaltyPointsResponse;
import com.mss.user_service.payloads.response.ProfileCompletionResponse;
import com.mss.user_service.payloads.response.UserProfileResponse;
import com.mss.user_service.service.AzureStorageService;
import com.mss.user_service.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User Profile Management", description = "APIs for managing user profiles")
@SecurityRequirement(name = "bearer-jwt")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;
    private final AzureStorageService azureStorageService;

    // ==================== USER ENDPOINTS ====================

    @GetMapping("/users/me")
    @Operation(summary = "Get current user profile", description = "Get current user's profile. Auto-create if doesn't exist (first login)")
    public ResponseEntity<BaseResponse> getCurrentUserProfile(@AuthenticationPrincipal Jwt jwt) {
        String keycloakUserId = jwt.getSubject();
        UserProfile profile = userProfileService.getOrCreateUserProfile(keycloakUserId, jwt);
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.ok(new BaseResponse(
                "User profile retrieved successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }

    @PostMapping("/users/me/complete")
    @Operation(summary = "Complete user profile", description = "User completes profile for the first time")
    public ResponseEntity<BaseResponse> completeUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CompleteProfileRequest request) {

        String keycloakUserId = jwt.getSubject();
        UserProfile profile = userProfileService.completeUserProfile(keycloakUserId, request);
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.ok(new BaseResponse(
                "Profile completed successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }

    @PutMapping("/users/me")
    @Operation(summary = "Update user profile", description = "Update profile information")
    public ResponseEntity<BaseResponse> updateUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request) {

        String keycloakUserId = jwt.getSubject();
        UserProfile profile = userProfileService.updateUserProfile(keycloakUserId, request);
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.ok(new BaseResponse(
                "Profile updated successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }

    @GetMapping("/users/me/completion-status")
    @Operation(summary = "Get profile completion status", description = "Check if profile is complete and what's missing")
    public ResponseEntity<BaseResponse> getProfileCompletionStatus(@AuthenticationPrincipal Jwt jwt) {
        String keycloakUserId = jwt.getSubject();
        ProfileCompletionResponse completionStatus = userProfileService.getProfileCompletionStatus(keycloakUserId);

        return ResponseEntity.ok(new BaseResponse(
                "Profile completion status retrieved successfully",
                String.valueOf(HttpStatus.OK.value()),
                completionStatus
        ));
    }

    // ==================== ADDRESS MANAGEMENT ====================

    @PostMapping("/users/me/addresses")
    @Operation(summary = "Add shipping address", description = "Add a new shipping address")
    public ResponseEntity<BaseResponse> addShippingAddress(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddressDto addressDto) {

        String keycloakUserId = jwt.getSubject();
        UserProfile profile = userProfileService.addShippingAddress(keycloakUserId, addressDto);
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.ok(new BaseResponse(
                "Shipping address added successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }

    @PutMapping("/users/me/addresses/{index}")
    @Operation(summary = "Update shipping address", description = "Update shipping address by index")
    public ResponseEntity<BaseResponse> updateShippingAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer index,
            @Valid @RequestBody AddressDto addressDto) {

        String keycloakUserId = jwt.getSubject();
        UserProfile profile = userProfileService.updateShippingAddress(keycloakUserId, index, addressDto);
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.ok(new BaseResponse(
                "Shipping address updated successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }

    @DeleteMapping("/users/me/addresses/{index}")
    @Operation(summary = "Delete shipping address", description = "Delete shipping address by index")
    public ResponseEntity<BaseResponse> deleteShippingAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer index) {

        String keycloakUserId = jwt.getSubject();
        UserProfile profile = userProfileService.deleteShippingAddress(keycloakUserId, index);
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.ok(new BaseResponse(
                "Shipping address deleted successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }

    @PatchMapping("/users/me/addresses/{index}/default")
    @Operation(summary = "Set default address", description = "Set a shipping address as default")
    public ResponseEntity<BaseResponse> setDefaultAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer index) {

        String keycloakUserId = jwt.getSubject();
        UserProfile profile = userProfileService.setDefaultAddress(keycloakUserId, index);
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.ok(new BaseResponse(
                "Default address set successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }

    // ==================== PREFERENCES & SETTINGS ====================

    @PutMapping("/users/me/preferences")
    @Operation(summary = "Update user preferences", description = "Update user preferences and settings")
    public ResponseEntity<BaseResponse> updatePreferences(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserPreferencesDto preferencesDto) {

        String keycloakUserId = jwt.getSubject();
        UserProfile profile = userProfileService.updatePreferences(keycloakUserId, preferencesDto);
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.ok(new BaseResponse(
                "Preferences updated successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }

    @GetMapping("/users/me/loyalty-points")
    @Operation(summary = "Get loyalty points", description = "Get current user's loyalty points")
    public ResponseEntity<BaseResponse> getLoyaltyPoints(@AuthenticationPrincipal Jwt jwt) {
        String keycloakUserId = jwt.getSubject();
        UserProfile profile = userProfileService.getUserProfile(keycloakUserId);

        LoyaltyPointsResponse loyaltyPoints = LoyaltyPointsResponse.builder()
                .loyaltyPoints(profile.getLoyaltyPoints())
                .build();

        return ResponseEntity.ok(new BaseResponse(
                "Loyalty points retrieved successfully",
                String.valueOf(HttpStatus.OK.value()),
                loyaltyPoints
        ));
    }

    // ==================== AVATAR MANAGEMENT ====================

    @PostMapping(value = "/users/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload avatar", description = "Upload user avatar image (jpg, png, webp, max 5MB)")
    public ResponseEntity<BaseResponse> uploadAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file) {

        validateImageFile(file);

        String keycloakUserId = jwt.getSubject();
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;

        UserProfile profile = userProfileService.getUserProfile(keycloakUserId);
        if (profile.getAvatarUrl() != null) {
            azureStorageService.deleteFile(profile.getAvatarUrl());
        }

        String avatarUrl = azureStorageService.uploadFile(file, keycloakUserId, filename);
        profile = userProfileService.updateAvatarUrl(keycloakUserId, avatarUrl);
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.ok(new BaseResponse(
                "Avatar uploaded successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }

    @DeleteMapping("/users/me/avatar")
    @Operation(summary = "Delete avatar", description = "Remove user avatar")
    public ResponseEntity<BaseResponse> deleteAvatar(@AuthenticationPrincipal Jwt jwt) {
        String keycloakUserId = jwt.getSubject();
        UserProfile profile = userProfileService.getUserProfile(keycloakUserId);

        if (profile.getAvatarUrl() != null) {
            azureStorageService.deleteFile(profile.getAvatarUrl());
        }

        profile = userProfileService.updateAvatarUrl(keycloakUserId, null);
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.ok(new BaseResponse(
                "Avatar deleted successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/admin/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users (Admin)", description = "Search users by keyword and filter by status with pagination")
    public ResponseEntity<BaseResponse> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfile> userPage = userProfileService.searchUsers(keyword, status, pageable);
        Page<UserProfileResponse> responsePage = userPage.map(userProfileMapper::toResponse);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", responsePage.getContent());
        responseData.put("page", responsePage.getNumber());
        responseData.put("size", responsePage.getSize());
        responseData.put("totalElements", responsePage.getTotalElements());
        responseData.put("totalPages", responsePage.getTotalPages());

        return ResponseEntity.ok(new BaseResponse(
                "Users retrieved successfully",
                String.valueOf(HttpStatus.OK.value()),
                responseData
        ));
    }

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user (Admin)", description = "Admin creates a new user profile")
    public ResponseEntity<BaseResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        UserProfile profile = userProfileService.adminCreateUser(request);
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse(
                "User created successfully",
                String.valueOf(HttpStatus.CREATED.value()),
                response
        ));
    }

    @GetMapping("/admin/users/{keycloakUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (Admin)", description = "Admin endpoint to get user profile by Keycloak user ID")
    public ResponseEntity<BaseResponse> getUserById(@PathVariable String keycloakUserId) {
        UserProfile profile = userProfileService.getUserProfile(keycloakUserId);
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.ok(new BaseResponse(
                "User profile retrieved successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users (Admin)", description = "Admin endpoint to list all users with pagination")
    public ResponseEntity<BaseResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfile> userPage = userProfileService.getAllUsers(pageable);

        Page<UserProfileResponse> responsePage = userPage.map(userProfileMapper::toResponse);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", responsePage.getContent());
        responseData.put("page", responsePage.getNumber());
        responseData.put("size", responsePage.getSize());
        responseData.put("totalElements", responsePage.getTotalElements());
        responseData.put("totalPages", responsePage.getTotalPages());

        return ResponseEntity.ok(new BaseResponse(
                "Users retrieved successfully",
                String.valueOf(HttpStatus.OK.value()),
                responseData
        ));
    }

    @DeleteMapping("/admin/users/{keycloakUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete user (Admin)", description = "Admin endpoint to soft delete a user")
    public ResponseEntity<BaseResponse> softDeleteUser(@PathVariable String keycloakUserId) {
        userProfileService.softDeleteUser(keycloakUserId);

        return ResponseEntity.ok(new BaseResponse(
                "User deleted successfully",
                String.valueOf(HttpStatus.OK.value()),
                null
        ));
    }

    // ==================== HELPER METHODS ====================

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");
        if (file.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("File size exceeds 5MB");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "jpg";
        int lastDot = filename.lastIndexOf(".");
        return lastDot > 0 ? filename.substring(lastDot + 1) : "jpg";
    }

    // ==================== INTERNAL ENDPOINTS ====================

    @PostMapping("/internal/users/{keycloakUserId}/loyalty-points")
    @PreAuthorize("hasRole('SERVICE')")
    @Operation(summary = "Add loyalty points (Internal)", description = "Internal API for inter-service communication to add loyalty points")
    public ResponseEntity<BaseResponse> addLoyaltyPoints(
            @PathVariable String keycloakUserId,
            @Valid @RequestBody AddLoyaltyPointsRequest request) {

        UserProfile profile = userProfileService.addLoyaltyPoints(
                keycloakUserId,
                request.getPoints(),
                request.getReason()
        );
        UserProfileResponse response = userProfileMapper.toResponse(profile);

        return ResponseEntity.ok(new BaseResponse(
                "Loyalty points added successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }
}

