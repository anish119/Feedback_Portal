package com.feedbackportal.service;

import com.feedbackportal.dto.RegistrationDto;
import com.feedbackportal.entity.User;
import com.feedbackportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.user-id}")
    private String adminUserId;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user.
     * @throws IllegalArgumentException if userId is taken or matches the admin ID.
     */
    public void register(RegistrationDto dto) {
        String userId = dto.getUserId().trim();

        // Block registration with the admin user ID
        if (userId.equalsIgnoreCase(adminUserId)) {
            throw new IllegalArgumentException("This user ID is reserved and cannot be used for registration.");
        }

        if (userRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("User ID '" + userId + "' is already taken. Please choose another.");
        }

        User user = new User();
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("USER"); // always USER — never allow choosing role
        userRepository.save(user);
    }

    /**
     * Returns matching user IDs for admin autocomplete (role USER only, excluding admin).
     * Limit 50, sorted alphabetically.
     */
    public List<String> getUserIds(String query) {
        PageRequest pageRequest = PageRequest.of(0, 50);
        if (query == null || query.isBlank()) {
            return userRepository.findAllUserIds(adminUserId, pageRequest);
        }
        String escaped = query.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        String pattern = "%" + escaped.toLowerCase() + "%";
        return userRepository.findUserIdsMatching(pattern, adminUserId, pageRequest);
    }
}
