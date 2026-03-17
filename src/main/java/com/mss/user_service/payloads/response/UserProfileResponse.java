package com.mss.user_service.payloads.response;

import com.mss.user_service.entity.Address;
import com.mss.user_service.entity.UserPreferences;
import com.mss.user_service.enums.Gender;
import com.mss.user_service.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
    private String keycloakUserId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private Boolean emailVerified;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private List<Address> shippingAddresses;
    private UserPreferences preferences;
    private Integer loyaltyPoints;
    private Boolean profileCompleted;
    private Double completionPercentage;
    private UserStatus status;
    private Integer totalOrders;
    private Double totalSpent;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}

