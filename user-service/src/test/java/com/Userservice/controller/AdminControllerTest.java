package com.Userservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.Userservice.client.ResumeClient;
import com.Userservice.dto.UserDTO;
import com.Userservice.entity.Role;
import com.Userservice.entity.User;
import com.Userservice.service.UserService;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ResumeClient resumeClient;

    @InjectMocks
    private AdminController adminController;

    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
    }

    @Test
    public void testGetAllUsers_Success() {
        when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser));

        ResponseEntity<List<UserDTO>> response = adminController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("USER", response.getBody().get(0).getRole());
    }

    @Test
    public void testDeleteUser_Success() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<String> response = adminController.deleteUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());
    }

    @Test
    public void testGetAllResumes_Success() {
        Object dummyResume = new Object();
        when(resumeClient.getAllResumes()).thenReturn(Arrays.asList(dummyResume));

        ResponseEntity<List<Object>> response = adminController.getAllResumes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testDeleteResume_Success() {
        doNothing().when(resumeClient).deleteResume(1L);

        ResponseEntity<String> response = adminController.deleteResume(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Resume deleted successfully", response.getBody());
    }
}
