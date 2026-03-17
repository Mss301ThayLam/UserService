package com.mss.user_service.payloads.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WishlistRequest {

    @NotBlank(message = "productId is required")
    private String productId;
}
