package com.Userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Userservice.entity.User;
import com.Userservice.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Email already exists!");
        }
        // Password hashing is now handled by auth-service before calling this endpoint.
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
   
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found!"));
    }

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void upgradeToPremium(Long id) {
        User user = getUserById(id);
        user.setRole(com.Userservice.entity.Role.PREMIUM_USER);
        user.setPremiumExpiryDate(java.time.LocalDateTime.now().plusMonths(1));
        userRepository.save(user);
    }

    public void downgradeToBasic(Long id) {
        User user = getUserById(id);
        user.setRole(com.Userservice.entity.Role.USER);
        user.setPremiumExpiryDate(null);
        userRepository.save(user);
    }
}
