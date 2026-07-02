package com.auth_service.service;

import com.auth_service.client.NotificationClient;
import com.auth_service.entity.OtpToken;
import com.auth_service.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${notification.internal.token}")
    private String notificationToken;

    private static final int OTP_EXPIRY_MINUTES = 5;

    @Transactional
    public void generateAndSendOtp(String email, String userName) {
        int otpValue = 100000 + secureRandom.nextInt(900000);
        String rawOtp = String.valueOf(otpValue);

        log.info("Generated OTP for email: {}", email);

        // Delete any existing OTP for this email
        otpTokenRepository.deleteByEmail(email);

        // Save hashed OTP
        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otpHash(passwordEncoder.encode(rawOtp))
                .expiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();
        otpTokenRepository.save(otpToken);

        // Send Email via Notification Service
        try {
            Map<String, Object> emailRequest = Map.of(
                    "to", email,
                    "subject", "Your Login OTP - Smart Resume Builder",
                    "templateName", "otp-email",
                    "variables", Map.of("username", userName != null ? userName : "User", "otp", rawOtp)
            );
            notificationClient.sendNotification("Bearer " + notificationToken, emailRequest);
            log.info("OTP sent to notification service for {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", email, e.getMessage());
            // Non-fatal: user still sees OTP prompt; can resend
        }
    }

    @Transactional
    public boolean validateOtp(String email, String rawOtp) {
        OtpToken otpToken = otpTokenRepository.findByEmail(email).orElse(null);

        if (otpToken == null) {
            log.warn("No OTP found for email: {}", email);
            return false;
        }

        if (otpToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for email: {}", email);
            otpTokenRepository.delete(otpToken);
            return false;
        }

        if (passwordEncoder.matches(rawOtp, otpToken.getOtpHash())) {
            log.info("OTP verified successfully for email: {}", email);
            otpTokenRepository.delete(otpToken); // Use once and destroy
            return true;
        }

        log.warn("Invalid OTP entered for email: {}", email);
        return false;
    }
}