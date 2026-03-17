package com.mss.user_service.controller;

import com.mss.user_service.entity.WishlistItem;
import com.mss.user_service.payloads.requests.WishlistRequest;
import com.mss.user_service.payloads.response.BaseResponse;
import com.mss.user_service.payloads.response.WishlistResponse;
import com.mss.user_service.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "APIs for managing user's wishlist")
@SecurityRequirement(name = "bearer-jwt")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get wishlist", description = "Get current user's wishlist")
    public ResponseEntity<BaseResponse> getWishlist(@AuthenticationPrincipal Jwt jwt) {
        String keycloakUserId = jwt.getSubject();
        List<WishlistItem> items = wishlistService.getWishlist(keycloakUserId);
        List<WishlistResponse> response = items.stream().map(this::toResponse).toList();

        return ResponseEntity.ok(new BaseResponse(
                "Wishlist retrieved successfully",
                String.valueOf(HttpStatus.OK.value()),
                response
        ));
    }

    @PostMapping
    @Operation(summary = "Add to wishlist", description = "Add a product to user's wishlist")
    public ResponseEntity<BaseResponse> addToWishlist(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WishlistRequest request) {

        String keycloakUserId = jwt.getSubject();
        WishlistItem item = wishlistService.addToWishlist(keycloakUserId, request.getProductId());

        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse(
                "Product added to wishlist successfully",
                String.valueOf(HttpStatus.CREATED.value()),
                toResponse(item)
        ));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove from wishlist", description = "Remove a product from user's wishlist")
    public ResponseEntity<BaseResponse> removeFromWishlist(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId) {

        String keycloakUserId = jwt.getSubject();
        wishlistService.removeFromWishlist(keycloakUserId, productId);

        return ResponseEntity.ok(new BaseResponse(
                "Product removed from wishlist successfully",
                String.valueOf(HttpStatus.OK.value()),
                null
        ));
    }

    @GetMapping("/check/{productId}")
    @Operation(summary = "Check wishlist", description = "Check if a product is in user's wishlist")
    public ResponseEntity<BaseResponse> checkWishlist(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId) {

        String keycloakUserId = jwt.getSubject();
        boolean inWishlist = wishlistService.isInWishlist(keycloakUserId, productId);

        return ResponseEntity.ok(new BaseResponse(
                "Wishlist check completed",
                String.valueOf(HttpStatus.OK.value()),
                Map.of("inWishlist", inWishlist, "productId", productId)
        ));
    }

    @GetMapping("/count")
    @Operation(summary = "Count wishlist", description = "Get count of products in user's wishlist")
    public ResponseEntity<BaseResponse> countWishlist(@AuthenticationPrincipal Jwt jwt) {
        String keycloakUserId = jwt.getSubject();
        long count = wishlistService.countWishlist(keycloakUserId);

        return ResponseEntity.ok(new BaseResponse(
                "Wishlist count retrieved successfully",
                String.valueOf(HttpStatus.OK.value()),
                Map.of("count", count)
        ));
    }

    private WishlistResponse toResponse(WishlistItem item) {
        return WishlistResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .addedAt(item.getAddedAt())
                .build();
    }
}
