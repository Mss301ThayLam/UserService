package com.mss.user_service.mapper;

import com.mss.user_service.dto.AddressDto;
import com.mss.user_service.dto.UserPreferencesDto;
import com.mss.user_service.entity.Address;
import com.mss.user_service.entity.UserPreferences;
import com.mss.user_service.entity.UserProfile;
import com.mss.user_service.payloads.response.UserProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public UserProfileResponse toResponse(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }

        return UserProfileResponse.builder()
                .id(userProfile.getId())
                .keycloakUserId(userProfile.getKeycloakUserId())
                .email(userProfile.getEmail())
                .username(userProfile.getUsername())
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .emailVerified(userProfile.getEmailVerified())
                .phoneNumber(userProfile.getPhoneNumber())
                .dateOfBirth(userProfile.getDateOfBirth())
                .gender(userProfile.getGender())
                .shippingAddresses(userProfile.getShippingAddresses())
                .preferences(userProfile.getPreferences())
                .loyaltyPoints(userProfile.getLoyaltyPoints())
                .profileCompleted(userProfile.getProfileCompleted())
                .completionPercentage(userProfile.getCompletionPercentage())
                .status(userProfile.getStatus())
                .totalOrders(userProfile.getTotalOrders())
                .totalSpent(userProfile.getTotalSpent())
                .avatarUrl(userProfile.getAvatarUrl())
                .createdAt(userProfile.getCreatedAt())
                .updatedAt(userProfile.getUpdatedAt())
                .lastLoginAt(userProfile.getLastLoginAt())
                .build();
    }

    public Address toAddress(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        return Address.builder()
                .label(addressDto.getLabel())
                .receiverName(addressDto.getReceiverName())
                .phoneNumber(addressDto.getPhoneNumber())
                .addressLine(addressDto.getAddressLine())
                .ward(addressDto.getWard())
                .district(addressDto.getDistrict())
                .city(addressDto.getCity())
                .postalCode(addressDto.getPostalCode())
                .isDefault(false)
                .build();
    }

    public UserPreferences toUserPreferences(UserPreferencesDto dto) {
        if (dto == null) {
            return UserPreferences.createDefault();
        }

        return UserPreferences.builder()
                .language(dto.getLanguage() != null ? dto.getLanguage() : "vi")
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "VND")
                .emailNotifications(dto.getEmailNotifications() != null ? dto.getEmailNotifications() : true)
                .smsNotifications(dto.getSmsNotifications() != null ? dto.getSmsNotifications() : false)
                .promotionalEmails(dto.getPromotionalEmails() != null ? dto.getPromotionalEmails() : true)
                .build();
    }
}

