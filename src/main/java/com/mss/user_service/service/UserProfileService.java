package com.mss.user_service.service;

import com.mss.user_service.dto.AddressDto;
import com.mss.user_service.dto.UserPreferencesDto;
import com.mss.user_service.entity.UserProfile;
import com.mss.user_service.payloads.requests.CompleteProfileRequest;
import com.mss.user_service.payloads.requests.UpdateProfileRequest;
import com.mss.user_service.payloads.response.ProfileCompletionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserProfileService {

    /**
     * Get user profile or create if not exists (for first login)
     */
    UserProfile getOrCreateUserProfile(String keycloakUserId, Jwt jwt);

    /**
     * Get user profile by keycloak user ID
     */
    UserProfile getUserProfile(String keycloakUserId);

    /**
     * Complete user profile for the first time
     */
    UserProfile completeUserProfile(String keycloakUserId, CompleteProfileRequest request);

    /**
     * Update user profile
     */
    UserProfile updateUserProfile(String keycloakUserId, UpdateProfileRequest request);

    /**
     * Get profile completion status
     */
    ProfileCompletionResponse getProfileCompletionStatus(String keycloakUserId);

    /**
     * Add shipping address
     */
    UserProfile addShippingAddress(String keycloakUserId, AddressDto addressDto);

    /**
     * Update shipping address by index
     */
    UserProfile updateShippingAddress(String keycloakUserId, Integer index, AddressDto addressDto);

    /**
     * Delete shipping address by index
     */
    UserProfile deleteShippingAddress(String keycloakUserId, Integer index);

    /**
     * Set default shipping address
     */
    UserProfile setDefaultAddress(String keycloakUserId, Integer index);

    /**
     * Update user preferences
     */
    UserProfile updatePreferences(String keycloakUserId, UserPreferencesDto preferencesDto);

    /**
     * Add loyalty points
     */
    UserProfile addLoyaltyPoints(String keycloakUserId, Integer points, String reason);

    /**
     * Get user by email (admin)
     */
    UserProfile getUserByEmail(String email);

    /**
     * Get all users with pagination (admin)
     */
    Page<UserProfile> getAllUsers(Pageable pageable);

    /**
     * Soft delete user (admin)
     */
    void softDeleteUser(String keycloakUserId);
}

