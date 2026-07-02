package com.resume_service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.resume_service.client.NotificationClient;
import com.resume_service.client.UserClient;
import com.resume_service.dto.ApiResponse;
import com.resume_service.dto.UserRequestResponseDTO;

@ExtendWith(MockitoExtension.class)
public class AsyncNotificationTaskServiceTest {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private AsyncNotificationTaskService asyncNotificationTaskService;

    private UserRequestResponseDTO testUser;

    @BeforeEach
    public void setup() {
        testUser = new UserRequestResponseDTO();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@test.com");
    }

    @Test
    public void testSendUpdateNotificationAsync_Success() {
        ApiResponse<UserRequestResponseDTO> response = ApiResponse.success("User found", testUser);
        when(userClient.getUserById(1L)).thenReturn(response);

        assertDoesNotThrow(() -> asyncNotificationTaskService.sendUpdateNotificationAsync(1L, 2));

        verify(notificationClient).sendNotification(any(Map.class));
    }

    @Test
    public void testSendUpdateNotificationAsync_UserFetchFailure() {
        when(userClient.getUserById(1L)).thenThrow(new RuntimeException("User not found"));

        assertDoesNotThrow(() -> asyncNotificationTaskService.sendUpdateNotificationAsync(1L, 2));
    }

    @Test
    public void testSendUpdateNotificationAsync_NotificationFailure() {
        ApiResponse<UserRequestResponseDTO> response = ApiResponse.success("User found", testUser);
        when(userClient.getUserById(1L)).thenReturn(response);
        doThrow(new RuntimeException("Notification failed")).when(notificationClient).sendNotification(any(Map.class));

        assertDoesNotThrow(() -> asyncNotificationTaskService.sendUpdateNotificationAsync(1L, 2));
    }
}
