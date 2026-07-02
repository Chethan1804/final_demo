package com.Userservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.Userservice.dto.ApiResponse;
import com.Userservice.dto.UserDTO;
import com.Userservice.entity.User;
import com.Userservice.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UserDTO userDTO;

    @BeforeEach
    public void setup() {
        testUser = new User(1L, "Test Name", "test@example.com", "password123", com.Userservice.entity.Role.USER, null);
        userDTO = new UserDTO(1L, "Test Name", "test@example.com", "password123", "USER");
    }

    @Test
    public void testCreateUser_Success() {
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        ResponseEntity<ApiResponse<UserDTO>> response = userController.createUser(userDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test@example.com", response.getBody().getData().getEmail());
    }

    @Test
    public void testGetUserByEmail_Success() {
        when(userService.getUserByEmail(any(String.class))).thenReturn(testUser);

        ResponseEntity<ApiResponse<UserDTO>> response = userController.getUserByEmail("test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test@example.com", response.getBody().getData().getEmail());
    }

    @Test
    public void testGetUserById_Success() {
        when(userService.getUserById(1L)).thenReturn(testUser);

        ResponseEntity<ApiResponse<UserDTO>> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getData().getId());
    }
}
