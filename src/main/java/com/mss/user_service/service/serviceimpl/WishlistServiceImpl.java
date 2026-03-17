package com.mss.user_service.service.serviceimpl;

import com.mss.user_service.entity.WishlistItem;
import com.mss.user_service.repository.WishlistRepository;
import com.mss.user_service.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;

    @Override
    public List<WishlistItem> getWishlist(String keycloakUserId) {
        log.info("Getting wishlist for keycloakUserId: {}", keycloakUserId);
        return wishlistRepository.findByKeycloakUserId(keycloakUserId);
    }

    @Override
    @Transactional
    public WishlistItem addToWishlist(String keycloakUserId, String productId) {
        log.info("Adding productId={} to wishlist for keycloakUserId: {}", productId, keycloakUserId);

        if (wishlistRepository.existsByKeycloakUserIdAndProductId(keycloakUserId, productId)) {
            return wishlistRepository.findByKeycloakUserIdAndProductId(keycloakUserId, productId)
                    .orElseThrow();
        }

        WishlistItem item = WishlistItem.builder()
                .keycloakUserId(keycloakUserId)
                .productId(productId)
                .addedAt(LocalDateTime.now())
                .build();

        return wishlistRepository.save(item);
    }

    @Override
    @Transactional
    public void removeFromWishlist(String keycloakUserId, String productId) {
        log.info("Removing productId={} from wishlist for keycloakUserId: {}", productId, keycloakUserId);
        wishlistRepository.deleteByKeycloakUserIdAndProductId(keycloakUserId, productId);
    }

    @Override
    public boolean isInWishlist(String keycloakUserId, String productId) {
        return wishlistRepository.existsByKeycloakUserIdAndProductId(keycloakUserId, productId);
    }

    @Override
    public long countWishlist(String keycloakUserId) {
        return wishlistRepository.countByKeycloakUserId(keycloakUserId);
    }
}
