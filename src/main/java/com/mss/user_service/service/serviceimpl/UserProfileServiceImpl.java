package com.mss.user_service.service.serviceimpl;

import com.mss.user_service.dto.AddressDto;
import com.mss.user_service.dto.UserPreferencesDto;
import com.mss.user_service.entity.Address;
import com.mss.user_service.entity.UserPreferences;
import com.mss.user_service.entity.UserProfile;
import com.mss.user_service.enums.Gender;
import com.mss.user_service.enums.UserStatus;
import com.mss.user_service.exceptions.InvalidAddressIndexException;
import com.mss.user_service.exceptions.MaxAddressLimitException;
import com.mss.user_service.exceptions.ProfileAlreadyCompletedException;
import com.mss.user_service.exceptions.UserNotFoundException;
import com.mss.user_service.mapper.UserProfileMapper;
import com.mss.user_service.payloads.requests.AdminCreateUserRequest;
import com.mss.user_service.payloads.requests.CompleteProfileRequest;
import com.mss.user_service.payloads.requests.UpdateProfileRequest;
import com.mss.user_service.payloads.response.ProfileCompletionResponse;
import com.mss.user_service.repository.UserProfileRepository;
import com.mss.user_service.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final MongoTemplate mongoTemplate;

    private static final int MAX_ADDRESSES = 5;

    @Override
    @Transactional
    public UserProfile getOrCreateUserProfile(String keycloakUserId, Jwt jwt) {
        log.info("Getting or creating user profile for keycloakUserId: {}", keycloakUserId);

        // Try to find existing profile
        return userProfileRepository.findByKeycloakUserId(keycloakUserId)
                .map(profile -> {
                    // Sync data from JWT
                    profile.setEmail(jwt.getClaim("email"));
                    profile.setFirstName(jwt.getClaim("given_name"));
                    profile.setLastName(jwt.getClaim("family_name"));
                    profile.setEmailVerified(jwt.getClaim("email_verified"));
                    profile.setLastLoginAt(LocalDateTime.now());
                    profile.setUpdatedAt(LocalDateTime.now());

                    // Recalculate profile completion
                    ProfileCompletionResponse completion = calculateProfileCompletion(profile);
                    profile.setCompletionPercentage(completion.getPercentage());
                    profile.setProfileCompleted(completion.getCompleted());

                    return userProfileRepository.save(profile);
                })
                .orElseGet(() -> {
                    // Create new profile
                    UserProfile newProfile = UserProfile.builder()
                            .keycloakUserId(keycloakUserId)
                            .email(jwt.getClaim("email"))
                            .username(jwt.getClaim("preferred_username"))
                            .firstName(jwt.getClaim("given_name"))
                            .lastName(jwt.getClaim("family_name"))
                            .emailVerified(jwt.getClaim("email_verified"))
                            .profileCompleted(false)
                            .completionPercentage(calculateInitialCompletion())
                            .status(UserStatus.ACTIVE)
                            .loyaltyPoints(0)
                            .totalOrders(0)
                            .totalSpent(0.0)
                            .shippingAddresses(new ArrayList<>())
                            .preferences(UserPreferences.createDefault())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .lastLoginAt(LocalDateTime.now())
                            .build();

                    log.info("Created new user profile for keycloakUserId: {}", keycloakUserId);
                    return userProfileRepository.save(newProfile);
                });
    }

    @Override
    public UserProfile getUserProfile(String keycloakUserId) {
        log.info("Getting user profile for keycloakUserId: {}", keycloakUserId);
        return userProfileRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found with keycloakUserId: " + keycloakUserId));
    }

    @Override
    @Transactional
    public UserProfile completeUserProfile(String keycloakUserId, CompleteProfileRequest request) {
        log.info("Completing user profile for keycloakUserId: {}", keycloakUserId);

        UserProfile profile = getUserProfile(keycloakUserId);

        // Check if profile already completed
        if (Boolean.TRUE.equals(profile.getProfileCompleted())) {
            throw new ProfileAlreadyCompletedException("Profile already completed");
        }

        // Update profile data
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());

        // Add first shipping address
        if (request.getShippingAddress() != null) {
            Address address = userProfileMapper.toAddress(request.getShippingAddress());
            address.setIsDefault(true); // First address is always default
            profile.getShippingAddresses().add(address);
        }

        // Update preferences
        if (request.getPreferences() != null) {
            profile.setPreferences(userProfileMapper.toUserPreferences(request.getPreferences()));
        }

        // Calculate completion
        ProfileCompletionResponse completion = calculateProfileCompletion(profile);
        profile.setCompletionPercentage(completion.getPercentage());
        profile.setProfileCompleted(completion.getCompleted());
        profile.setUpdatedAt(LocalDateTime.now());

        log.info("Profile completed for keycloakUserId: {}, completion: {}%", keycloakUserId, completion.getPercentage());
        return userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public UserProfile updateUserProfile(String keycloakUserId, UpdateProfileRequest request) {
        log.info("Updating user profile for keycloakUserId: {}", keycloakUserId);

        UserProfile profile = getUserProfile(keycloakUserId);

        // Update only provided fields
        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }

        // Recalculate completion
        ProfileCompletionResponse completion = calculateProfileCompletion(profile);
        profile.setCompletionPercentage(completion.getPercentage());
        profile.setProfileCompleted(completion.getCompleted());
        profile.setUpdatedAt(LocalDateTime.now());

        return userProfileRepository.save(profile);
    }

    @Override
    public ProfileCompletionResponse getProfileCompletionStatus(String keycloakUserId) {
        log.info("Getting profile completion status for keycloakUserId: {}", keycloakUserId);
        UserProfile profile = getUserProfile(keycloakUserId);
        return calculateProfileCompletion(profile);
    }

    @Override
    @Transactional
    public UserProfile addShippingAddress(String keycloakUserId, AddressDto addressDto) {
        log.info("Adding shipping address for keycloakUserId: {}", keycloakUserId);

        UserProfile profile = getUserProfile(keycloakUserId);

        // Check max addresses limit
        if (profile.getShippingAddresses().size() >= MAX_ADDRESSES) {
            throw new MaxAddressLimitException("Maximum " + MAX_ADDRESSES + " addresses allowed");
        }

        Address address = userProfileMapper.toAddress(addressDto);

        // If this is the first address, set as default
        if (profile.getShippingAddresses().isEmpty()) {
            address.setIsDefault(true);
        }

        profile.getShippingAddresses().add(address);
        profile.setUpdatedAt(LocalDateTime.now());

        // Recalculate completion
        ProfileCompletionResponse completion = calculateProfileCompletion(profile);
        profile.setCompletionPercentage(completion.getPercentage());
        profile.setProfileCompleted(completion.getCompleted());

        return userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public UserProfile updateShippingAddress(String keycloakUserId, Integer index, AddressDto addressDto) {
        log.info("Updating shipping address at index {} for keycloakUserId: {}", index, keycloakUserId);

        UserProfile profile = getUserProfile(keycloakUserId);
        List<Address> addresses = profile.getShippingAddresses();

        // Validate index
        if (index < 0 || index >= addresses.size()) {
            throw new InvalidAddressIndexException("Invalid address index: " + index);
        }

        Address existingAddress = addresses.get(index);
        Address updatedAddress = userProfileMapper.toAddress(addressDto);

        // Preserve default flag
        updatedAddress.setIsDefault(existingAddress.getIsDefault());

        // Update address at index
        addresses.set(index, updatedAddress);
        profile.setUpdatedAt(LocalDateTime.now());

        return userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public UserProfile deleteShippingAddress(String keycloakUserId, Integer index) {
        log.info("Deleting shipping address at index {} for keycloakUserId: {}", index, keycloakUserId);

        UserProfile profile = getUserProfile(keycloakUserId);
        List<Address> addresses = profile.getShippingAddresses();

        // Validate index
        if (index < 0 || index >= addresses.size()) {
            throw new InvalidAddressIndexException("Invalid address index: " + index);
        }

        // Cannot delete last address
        if (addresses.size() == 1) {
            throw new InvalidAddressIndexException("Cannot delete the last address. At least one address is required.");
        }

        Address addressToDelete = addresses.get(index);
        addresses.remove(index.intValue());

        // If deleted address was default, set first address as default
        if (Boolean.TRUE.equals(addressToDelete.getIsDefault()) && !addresses.isEmpty()) {
            addresses.get(0).setIsDefault(true);
        }

        profile.setUpdatedAt(LocalDateTime.now());

        // Recalculate completion
        ProfileCompletionResponse completion = calculateProfileCompletion(profile);
        profile.setCompletionPercentage(completion.getPercentage());
        profile.setProfileCompleted(completion.getCompleted());

        return userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public UserProfile setDefaultAddress(String keycloakUserId, Integer index) {
        log.info("Setting default address at index {} for keycloakUserId: {}", index, keycloakUserId);

        UserProfile profile = getUserProfile(keycloakUserId);
        List<Address> addresses = profile.getShippingAddresses();

        // Validate index
        if (index < 0 || index >= addresses.size()) {
            throw new InvalidAddressIndexException("Invalid address index: " + index);
        }

        // Set all addresses to non-default
        addresses.forEach(addr -> addr.setIsDefault(false));

        // Set selected address as default
        addresses.get(index).setIsDefault(true);
        profile.setUpdatedAt(LocalDateTime.now());

        return userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public UserProfile updatePreferences(String keycloakUserId, UserPreferencesDto preferencesDto) {
        log.info("Updating preferences for keycloakUserId: {}", keycloakUserId);

        UserProfile profile = getUserProfile(keycloakUserId);
        UserPreferences preferences = userProfileMapper.toUserPreferences(preferencesDto);

        profile.setPreferences(preferences);
        profile.setUpdatedAt(LocalDateTime.now());

        return userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public UserProfile addLoyaltyPoints(String keycloakUserId, Integer points, String reason) {
        log.info("Adding {} loyalty points for keycloakUserId: {}. Reason: {}", points, keycloakUserId, reason);

        UserProfile profile = getUserProfile(keycloakUserId);

        Integer currentPoints = profile.getLoyaltyPoints() != null ? profile.getLoyaltyPoints() : 0;
        profile.setLoyaltyPoints(currentPoints + points);
        profile.setUpdatedAt(LocalDateTime.now());

        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile getUserByEmail(String email) {
        log.info("Getting user profile by email: {}", email);
        return userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    public Page<UserProfile> getAllUsers(Pageable pageable) {
        log.info("Getting all users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return userProfileRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void softDeleteUser(String keycloakUserId) {
        log.info("Soft deleting user with keycloakUserId: {}", keycloakUserId);

        UserProfile profile = getUserProfile(keycloakUserId);

        // Set status to deleted
        profile.setStatus(UserStatus.DELETED);
        profile.setDeletedAt(LocalDateTime.now());

        // Anonymize email
        String anonymizedEmail = profile.getEmail() + ".deleted." + System.currentTimeMillis();
        profile.setEmail(anonymizedEmail);
        profile.setUpdatedAt(LocalDateTime.now());

        userProfileRepository.save(profile);
        log.info("User soft deleted successfully: {}", keycloakUserId);
    }

    /**
     * Calculate profile completion percentage and status
     */
    private ProfileCompletionResponse calculateProfileCompletion(UserProfile profile) {
        List<String> missingFields = new ArrayList<>();
        int totalFields = 7;
        int completedFields = 0;

        // Required fields from Keycloak (always present)
        if (profile.getEmail() != null) completedFields++;
        if (profile.getFirstName() != null) completedFields++;
        if (profile.getLastName() != null) completedFields++;

        // Additional required fields
        if (profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty()) {
            completedFields++;
        } else {
            missingFields.add("phoneNumber");
        }

        // Optional fields
        if (profile.getDateOfBirth() != null) {
            completedFields++;
        } else {
            missingFields.add("dateOfBirth");
        }

        if (profile.getGender() != null) {
            completedFields++;
        } else {
            missingFields.add("gender");
        }

        // Shipping address (critical)
        if (profile.getShippingAddresses() != null && !profile.getShippingAddresses().isEmpty()) {
            completedFields++;
        } else {
            missingFields.add("shippingAddress");
        }

        double percentage = ((double) completedFields / totalFields) * 100;
        boolean completed = profile.getPhoneNumber() != null
                && !profile.getPhoneNumber().isEmpty()
                && profile.getShippingAddresses() != null
                && !profile.getShippingAddresses().isEmpty();

        return ProfileCompletionResponse.builder()
                .completed(completed)
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .missingFields(missingFields)
                .build();
    }

    /**
     * Calculate initial completion percentage for new users
     */
    private Double calculateInitialCompletion() {
        // New user has: email, firstName, lastName = 3/7 fields
        return Math.round((3.0 / 7.0) * 100 * 100.0) / 100.0;
    }

    @Override
    @Transactional
    public UserProfile updateAvatarUrl(String keycloakUserId, String avatarUrl) {
        log.info("Updating avatar URL for keycloakUserId: {}", keycloakUserId);
        UserProfile profile = getUserProfile(keycloakUserId);
        profile.setAvatarUrl(avatarUrl);
        profile.setUpdatedAt(LocalDateTime.now());
        return userProfileRepository.save(profile);
    }

    @Override
    public Page<UserProfile> searchUsers(String keyword, String status, Pageable pageable) {
        log.info("Searching users with keyword={}, status={}", keyword, status);

        Criteria criteria = new Criteria();
        List<Criteria> conditions = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            String regex = ".*" + Pattern.quote(keyword) + ".*";
            conditions.add(new Criteria().orOperator(
                    Criteria.where("username").regex(regex, "i"),
                    Criteria.where("email").regex(regex, "i"),
                    Criteria.where("firstName").regex(regex, "i"),
                    Criteria.where("lastName").regex(regex, "i")
            ));
        }

        if (status != null && !status.isBlank()) {
            conditions.add(Criteria.where("status").is(status));
        }

        if (!conditions.isEmpty()) {
            criteria.andOperator(conditions.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria).with(pageable);
        List<UserProfile> users = mongoTemplate.find(query, UserProfile.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), UserProfile.class);

        return new PageImpl<>(users, pageable, total);
    }

    @Override
    @Transactional
    public UserProfile adminCreateUser(AdminCreateUserRequest request) {
        log.info("Admin creating user with username: {}, email: {}", request.getUsername(), request.getEmail());

        UserProfile profile = UserProfile.builder()
                .keycloakUserId(null)
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .gender(request.getGender() != null ? Gender.valueOf(request.getGender()) : null)
                .dateOfBirth(request.getDateOfBirth() != null ? LocalDate.parse(request.getDateOfBirth()) : null)
                .emailVerified(false)
                .profileCompleted(false)
                .completionPercentage(0.0)
                .status(UserStatus.ACTIVE)
                .loyaltyPoints(0)
                .totalOrders(0)
                .totalSpent(0.0)
                .shippingAddresses(new ArrayList<>())
                .preferences(UserPreferences.createDefault())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userProfileRepository.save(profile);
    }
}

