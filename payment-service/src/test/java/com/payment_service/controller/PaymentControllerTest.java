package com.payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.payment_service.dto.OrderRequestDTO;
import com.payment_service.dto.OrderResponseDTO;
import com.payment_service.dto.PaymentVerificationDTO;

import com.payment_service.service.PaymentService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateOrder() throws Exception {

        OrderRequestDTO request =
        		new OrderRequestDTO(
        		        "user123",
        		        "test@gmail.com",
        		        new BigDecimal("500.0"),
        		        "INR",
        		        "temp_id"
        		);

        OrderResponseDTO response =
                new OrderResponseDTO(
                        "order_123",
                        new BigDecimal("500.0"),
                        "INR"
                );

        when(paymentService.createOrder(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/payments/create-order")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void testVerifyPaymentSuccess() throws Exception {

        PaymentVerificationDTO request =
                new PaymentVerificationDTO(
                        "order_123",
                        "pay_123",
                        "sig_123"
                );

        when(paymentService.verifyPayment(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(true);

        mockMvc.perform(
                        post("/api/payments/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Payment verified and user upgraded to PREMIUM"));
    }

    @Test
    void testVerifyPaymentFailure() throws Exception {

        PaymentVerificationDTO request =
                new PaymentVerificationDTO(
                        "order_123",
                        "pay_123",
                        "sig_123"
                );

        when(paymentService.verifyPayment(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(false);

        mockMvc.perform(
                        post("/api/payments/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }
}