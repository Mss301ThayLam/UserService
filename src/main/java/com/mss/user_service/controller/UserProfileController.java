package com.mss.user_service.controller;

import com.mss.user_service.dto.AddressDto;
import com.mss.user_service.dto.UserPreferencesDto;
import com.mss.user_service.entity.UserProfile;
import com.mss.user_service.mapper.UserProfileMapper;
import com.mss.user_service.payloads.requests.AddLoyaltyPointsRequest;
import com.mss.user_service.payloads.requests.CompleteProfileRequest;
import com.mss.user_service.payloads.requests.UpdateProfileRequest;
import com.mss.user_service.payloads.response.BaseResponse;
import com.mss.user_service.payloads.response.LoyaltyPointsResponse;
import com.mss.user_service.payloads.response.ProfileCompletionResponse;
import com.mss.user_service.payloads.response.UserProfileResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User Profile Management", description = "APIs for managing user profiles")
@SecurityRequirement(name = "bearer-jwt")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;

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

    // ==================== ADMIN ENDPOINTS ====================

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

