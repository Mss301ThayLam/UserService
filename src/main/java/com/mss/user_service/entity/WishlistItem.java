package com.mss.user_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wishlists")
@CompoundIndex(name = "user_product_idx", def = "{'keycloakUserId': 1, 'productId': 1}", unique = true)
public class WishlistItem {

    @Id
    private String id;

    private String keycloakUserId;
    private String productId;
    private LocalDateTime addedAt;
}
