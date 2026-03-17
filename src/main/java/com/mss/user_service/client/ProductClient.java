package com.mss.user_service.client;

import com.mss.user_service.config.FeignClientInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Example ProductClient to communicate with ProductService.
 * Name must match application name of ProductService registered in Eureka ("PRODUCTSERVICE").
 */
@FeignClient(name = "PRODUCTSERVICE", path = "/api/v1/products", configuration = FeignClientInterceptor.class)
public interface ProductClient {

    // Add required API method signatures here when needed.

}
