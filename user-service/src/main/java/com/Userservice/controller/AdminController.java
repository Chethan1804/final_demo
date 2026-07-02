package com.Userservice.controller;

import com.Userservice.client.ResumeClient;
import com.Userservice.dto.UserDTO;
import com.Userservice.entity.User;
import com.Userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ResumeClient resumeClient;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> dtos = users.stream()
                .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), null, u.getRole().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/users/{userId}/resumes")
    public ResponseEntity<List<Object>> getResumesByUser(@PathVariable Long userId) {
        try {
            List<Object> resumes = resumeClient.getResumesByUserId(userId, String.valueOf(userId));
            return ResponseEntity.ok(resumes);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/resumes")
    public ResponseEntity<List<Object>> getAllResumes() {
        return ResponseEntity.ok(resumeClient.getAllResumes());
    }

    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<String> deleteResume(@PathVariable Long id) {
        resumeClient.deleteResume(id);
        return ResponseEntity.ok("Resume deleted successfully");
    }
}
