package com.payment_service.controller;

import com.payment_service.dto.ApiResponse;
import com.payment_service.dto.OrderRequestDTO;
import com.payment_service.dto.OrderResponseDTO;
import com.payment_service.dto.PaymentVerificationDTO;
import com.payment_service.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Razorpay payment order creation and verification")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @Operation(
        summary = "Create Razorpay order",
        description = "Creates a Razorpay payment order. Requires authenticated user. " +
                      "Returns orderId to pass to Razorpay checkout on the frontend."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Razorpay API error")
    })
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(
            @Valid @RequestBody OrderRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        // Enrich request from gateway-propagated headers if not provided in body
        if (userId != null && (request.getUserId() == null || request.getUserId().isBlank())) {
            request.setUserId(userId);
        }
        if (userEmail != null && (request.getEmail() == null || request.getEmail().isBlank())) {
            request.setEmail(userEmail);
        }

        log.info("Creating order for user={}, amount={}", request.getUserId(), request.getAmount());

        OrderResponseDTO response = paymentService.createOrder(request);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", response));
    }

    @PostMapping("/verify")
    @Operation(
        summary = "Verify payment and upgrade user to Premium",
        description = "Verifies Razorpay payment signature. On success, upgrades the user to PREMIUM_USER role " +
                      "and sends a confirmation email."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment verified, user upgraded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Signature mismatch")
    })
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyPayment(
            @Valid @RequestBody PaymentVerificationDTO request
    ) {
        log.info("Verifying payment for orderId={}", request.getRazorpayOrderId());

        boolean isSuccess = paymentService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (isSuccess) {
            return ResponseEntity.ok(
                ApiResponse.success("Payment verified and user upgraded to PREMIUM", Map.of("status", "SUCCESS"))
            );
        }

        return ResponseEntity.badRequest().body(
            ApiResponse.error("Payment verification failed", "Signature mismatch or invalid payment")
        );
    }
}