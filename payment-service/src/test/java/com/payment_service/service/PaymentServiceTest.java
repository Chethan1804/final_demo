package com.payment_service.service;

import com.payment_service.client.UserServiceClient;
import com.payment_service.dto.PaymentVerificationDTO;
import com.payment_service.entity.PaymentDetails;
import com.payment_service.entity.PaymentStatus;
import com.payment_service.exception.InvalidSignatureException;
import com.payment_service.exception.PaymentProcessingException;
import com.payment_service.repository.PaymentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
                paymentService,
                "webhookSecret",
                "dummySecret"
        );
    }

    @Test
    void testVerifyPayment_OrderNotFound_ThrowsException() {

        PaymentVerificationDTO request =
                new PaymentVerificationDTO(
                        "order_123",
                        "pay_123",
                        "sig_123"
                );

        when(paymentRepository.findByRazorpayOrderId("order_123"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> paymentService.verifyPayment(
                        request.getRazorpayOrderId(),
                        request.getRazorpayPaymentId(),
                        request.getRazorpaySignature()
                )
        );
    }

    @Test
    void testVerifyPayment_AlreadySuccess_ReturnsTrue() {

        PaymentVerificationDTO request =
                new PaymentVerificationDTO(
                        "order_123",
                        "pay_123",
                        "sig_123"
                );

        PaymentDetails payment = new PaymentDetails();

        payment.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findByRazorpayOrderId("order_123"))
                .thenReturn(Optional.of(payment));

        assertTrue(
                paymentService.verifyPayment(
                        request.getRazorpayOrderId(),
                        request.getRazorpayPaymentId(),
                        request.getRazorpaySignature()
                )
        );

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void testProcessWebhook_InvalidSignature_ThrowsException() {

        String payload = "{\"event\":\"payment.failed\"}";

        String signature = "invalid";

        assertThrows(
                InvalidSignatureException.class,
                () -> paymentService.processWebhook(payload, signature)
        );
    }
}