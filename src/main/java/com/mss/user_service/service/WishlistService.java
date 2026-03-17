package com.mss.user_service.service;

import com.mss.user_service.entity.WishlistItem;

import java.util.List;

public interface WishlistService {

    List<WishlistItem> getWishlist(String keycloakUserId);

    WishlistItem addToWishlist(String keycloakUserId, String productId);

    void removeFromWishlist(String keycloakUserId, String productId);

    boolean isInWishlist(String keycloakUserId, String productId);

    long countWishlist(String keycloakUserId);
}
