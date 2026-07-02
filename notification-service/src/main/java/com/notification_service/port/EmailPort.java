package com.notification_service.port;

import com.notification_service.dto.EmailRequestDTO;

import java.util.concurrent.CompletableFuture;

/**
 * Port interface for Email sending logic. Clean architecture ensures the 
 * business logic depends on this port rather than specific SMTP or API adapters.
 */
public interface EmailPort {
    
    /**
     * Dispatch an email asynchronously.
     * @param request The data packet containing template variables and recipient info.
     */
    void sendEmail(EmailRequestDTO request);
    
}
