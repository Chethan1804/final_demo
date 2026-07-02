package com.payment_service.service;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payment_service.client.NotificationClient;
import com.payment_service.client.UserServiceClient;
import com.payment_service.dto.EmailRequestDTO;
import com.payment_service.dto.OrderRequestDTO;
import com.payment_service.dto.OrderResponseDTO;
import com.payment_service.entity.PaymentDetails;
import com.payment_service.entity.PaymentStatus;
import com.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final NotificationClient notificationClient;
    private final UserServiceClient userServiceClient;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${notification.internal.token}")
    private String internalToken;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        try {
            JSONObject options = new JSONObject();
            // Razorpay requires amount in paise (multiply by 100)
            options.put("amount", request.getAmount().multiply(java.math.BigDecimal.valueOf(100)).intValue());
            options.put("currency", request.getCurrency());
            options.put("receipt", "receipt_" + System.currentTimeMillis());

            Order order = razorpayClient.orders.create(options);

            PaymentDetails payment = PaymentDetails.builder()
                    .userId(request.getUserId())
                    .email(request.getEmail())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .razorpayOrderId(order.get("id"))
                    .status(PaymentStatus.PENDING)
                    .build();

            paymentRepository.save(payment);

            log.info("Created Razorpay order {} for user {}", order.get("id"), request.getUserId());

            return new OrderResponseDTO(
                    order.get("id"),
                    request.getAmount(),
                    request.getCurrency()
            );

        } catch (Exception e) {
            log.error("Order creation failed for user {}: {}", request.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    @Transactional
    public boolean verifyPayment(String orderId, String paymentId, String signature) {

        PaymentDetails payment = paymentRepository
                .findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (isValid) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setRazorpayPaymentId(paymentId);
                payment.setRazorpaySignature(signature);
                paymentRepository.save(payment);

                // ✅ CRITICAL: upgrade user to PREMIUM in user-service
                upgradeUserToPremium(payment.getUserId());

                // Fire-and-forget email notification
                sendNotificationSafely(payment, "premium-upgrade", "Premium Upgrade Successful", null);

                log.info("Payment verified successfully for order {}, user upgraded to PREMIUM", orderId);
                return true;

            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                sendNotificationSafely(payment, "payment-failed", "Payment Verification Failed", "Signature verification failed");

                log.warn("Payment signature invalid for order {}", orderId);
                return false;
            }

        } catch (Exception e) {
            log.error("Exception during payment verification for order {}: {}", orderId, e.getMessage());

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            sendNotificationSafely(payment, "payment-failed", "Payment Error", e.getMessage());
            return false;
        }
    }

    @Transactional
    public void processWebhook(String payload, String signature) {
        try {
            if (!Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
                log.error("Invalid webhook signature received");
                return;
            }

            JSONObject jsonPayload = new JSONObject(payload);
            String event = jsonPayload.getString("event");

            String orderId = jsonPayload
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity")
                    .getString("order_id");

            paymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
                if ("payment.captured".equals(event) && payment.getStatus() != PaymentStatus.SUCCESS) {
                    payment.setStatus(PaymentStatus.SUCCESS);
                    paymentRepository.save(payment);

                    // Upgrade user via webhook too (idempotent — user-service handles duplicate gracefully)
                    upgradeUserToPremium(payment.getUserId());

                    sendNotificationSafely(payment, "premium-upgrade", "Premium Upgrade Successful", null);
                    log.info("Webhook: payment captured for order {}", orderId);

                } else if ("payment.failed".equals(event) && payment.getStatus() != PaymentStatus.FAILED) {
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);

                    sendNotificationSafely(payment, "payment-failed", "Payment Failed", "Declined by gateway");
                    log.info("Webhook: payment failed for order {}", orderId);
                }
            });

        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage());
        }
    }

    // ─── PRIVATE HELPERS ────────────────────────────────────────

    private void upgradeUserToPremium(String userId) {
        try {
            userServiceClient.upgradeToPremium(userId, "Bearer " + internalToken);
            log.info("User {} upgraded to PREMIUM_USER", userId);
        } catch (Exception e) {
            // Log but don't fail — payment is already recorded as SUCCESS
            // Admin can manually upgrade if this fails
            log.error("Failed to upgrade user {} to PREMIUM via user-service: {}", userId, e.getMessage());
        }
    }

    private void sendNotificationSafely(PaymentDetails payment, String template, String subject, String reason) {
        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("username", payment.getUserId());
            vars.put("orderId", payment.getRazorpayOrderId());
            vars.put("planName", "Premium Tier");
            vars.put("validUntil", "Lifetime");
            vars.put("amount", payment.getAmount().toPlainString());
            if (reason != null) {
                vars.put("reason", reason);
            }

            EmailRequestDTO request = EmailRequestDTO.builder()
                    .toEmail(payment.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .variables(vars)
                    .build();

            notificationClient.sendEmail("Bearer " + internalToken, request);

        } catch (Exception e) {
            // Fire-and-forget: log but never fail the payment flow due to email
            log.error("Fire-and-forget email failed for order {}: {}", payment.getRazorpayOrderId(), e.getMessage());
        }
    }
}