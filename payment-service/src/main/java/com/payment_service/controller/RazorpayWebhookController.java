package com.payment_service.controller;

import com.payment_service.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Razorpay webhooks")
public class RazorpayWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    @Operation(
        summary = "Razorpay webhook receiver",
        description = "Receives payment.captured and payment.failed events from Razorpay. " +
                      "No JWT required — validated by Razorpay webhook signature instead."
    )
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        log.info("Received Razorpay webhook event");
        paymentService.processWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}