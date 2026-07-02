package com.Userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.Userservice.dto.ApiResponse;
import com.Userservice.dto.UserDTO;
import com.Userservice.entity.Role;
import com.Userservice.entity.User;
import com.Userservice.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody @Valid UserDTO dto) {
        Role role = Role.USER;
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            String r = dto.getRole().replace("ROLE_", "").trim().toUpperCase();
            try { role = Role.valueOf(r); } catch (Exception ignored) {}
        }

        User user = new User(
                dto.getId(),
                dto.getName(),
                dto.getEmail(),
                dto.getPassword(),
                role,
                null
        );

        User savedUser = userService.createUser(user);
        UserDTO responseDto = new UserDTO(
                savedUser.getId(), savedUser.getName(),
                savedUser.getEmail(), null,
                savedUser.getRole().name()
        );
        return new ResponseEntity<>(ApiResponse.success("User created successfully", responseDto), HttpStatus.CREATED);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        UserDTO responseDto = new UserDTO(
                user.getId(), user.getName(),
                user.getEmail(), user.getPassword(),
                user.getRole().name()
        );
        return ResponseEntity.ok(ApiResponse.success("User found", responseDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserDTO safeDto = new UserDTO(
                user.getId(), user.getName(),
                user.getEmail(), null,
                user.getRole().name()
        );
        return ResponseEntity.ok(ApiResponse.success("User found", safeDto));
    }

    @PutMapping("/{id}/upgrade")
    public ResponseEntity<ApiResponse<Void>> upgradeToPremium(@PathVariable Long id) {
        userService.upgradeToPremium(id);
        return ResponseEntity.ok(ApiResponse.success("User upgraded to premium", null));
    }

    @PutMapping("/{id}/downgrade")
    public ResponseEntity<ApiResponse<Void>> downgradeToBasic(@PathVariable Long id) {
        userService.downgradeToBasic(id);
        return ResponseEntity.ok(ApiResponse.success("User downgraded to basic", null));
    }
}