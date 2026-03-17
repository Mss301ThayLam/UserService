package com.mss.user_service.client;

import com.mss.user_service.config.FeignClientInterceptor;
import com.mss.user_service.payloads.requests.CartRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Client to communicate with OrderService.
 * Name must match application name of OrderService registered in Eureka.
 */
@FeignClient(name = "ORDERSERVICE", path = "/api/v1/carts", configuration = FeignClientInterceptor.class)
public interface OrderClient {

    @PostMapping
    ResponseEntity<Object> createCart(@RequestBody CartRequest request);

}
