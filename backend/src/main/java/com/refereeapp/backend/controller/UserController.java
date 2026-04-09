package com.refereeapp.backend.controller;

import com.refereeapp.backend.model.User;
import com.refereeapp.backend.repository.UserRepository;
import com.refereeapp.backend.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.refereeapp.backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService; 
    private final PasswordEncoder passwordEncoder; 
    private final JwtUtil jwtUtil;

    public UserController(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/register")
    public User createUser(@RequestBody User user, @RequestParam(required = false) String secretKey) {
        // Логіка перевірки ролей та ключів
        if ("ADMIN".equals(user.getRole())) {
            if (!"REFMATE-2026".equals(secretKey)) {
                throw new RuntimeException("Неправильний секретний ключ адміністратора!");
            }
        } else {
            user.setRole("REFEREE");
        }
        
        // ШИФРУЄМО ПАРОЛЬ перед тим, як зберегти в базу
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userRepository.save(user);
    }

    @GetMapping("/available")
    public List<User> getAvailableReferees(@RequestParam String date) {
        return userService.getAvailableReferees(date);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User loginData) {
        User user = userRepository.findByEmail(loginData.getEmail())
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        if (!passwordEncoder.matches(loginData.getPassword(), user.getPassword())) {
            throw new RuntimeException("Неправильний пароль");
        }

        // ГЕНЕРУЄМО ТОКЕН
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());

        // Формуємо відповідь: віддаємо і токен, і дані користувача
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setAvailability(updatedUser.getAvailability());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("Користувача не знайдено"));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
}