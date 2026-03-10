package com.mss.user_service.repository;

import com.mss.user_service.entity.UserProfile;
import com.mss.user_service.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends MongoRepository<UserProfile, String> {

    Optional<UserProfile> findByKeycloakUserId(String keycloakUserId);

    Optional<UserProfile> findByEmail(String email);

    Boolean existsByKeycloakUserId(String keycloakUserId);

    Page<UserProfile> findByStatus(UserStatus status, Pageable pageable);

    void deleteByKeycloakUserId(String keycloakUserId);
}

