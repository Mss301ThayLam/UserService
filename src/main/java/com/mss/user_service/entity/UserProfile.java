package com.mss.user_service.entity;

import com.mss.user_service.enums.Gender;
import com.mss.user_service.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_profiles")
public class UserProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String keycloakUserId;

    @Indexed
    @Email
    private String email;

    private String username;
    private String firstName;
    private String lastName;
    private Boolean emailVerified;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
    private String phoneNumber;

    private LocalDate dateOfBirth;
    private Gender gender;

    @Builder.Default
    private List<Address> shippingAddresses = new ArrayList<>();

    @Builder.Default
    private UserPreferences preferences = UserPreferences.createDefault();

    @Builder.Default
    private Integer loyaltyPoints = 0;

    @Builder.Default
    private Boolean profileCompleted = false;

    @Builder.Default
    private Double completionPercentage = 0.0;

    @Indexed
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    // Statistics
    @Builder.Default
    private Integer totalOrders = 0;

    @Builder.Default
    private Double totalSpent = 0.0;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime deletedAt;
}

