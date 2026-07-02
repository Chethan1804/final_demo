package com.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @PutMapping("/api/users/{userId}/upgrade")
    ResponseEntity<Void> upgradeToPremium(@PathVariable("userId") String userId, @RequestHeader("Authorization") String token);
    
    @PutMapping("/api/users/{userId}/downgrade")
    ResponseEntity<Void> downgradeToBasic(@PathVariable("userId") String userId, @RequestHeader("Authorization") String token);
}
