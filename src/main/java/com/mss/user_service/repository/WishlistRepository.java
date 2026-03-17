package com.mss.user_service.repository;

import com.mss.user_service.entity.WishlistItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends MongoRepository<WishlistItem, String> {

    List<WishlistItem> findByKeycloakUserId(String keycloakUserId);

    Optional<WishlistItem> findByKeycloakUserIdAndProductId(String keycloakUserId, String productId);

    boolean existsByKeycloakUserIdAndProductId(String keycloakUserId, String productId);

    void deleteByKeycloakUserIdAndProductId(String keycloakUserId, String productId);

    long countByKeycloakUserId(String keycloakUserId);
}
