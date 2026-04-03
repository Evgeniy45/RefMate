package com.refereeapp.backend.controller;

import com.refereeapp.backend.model.User;
import com.refereeapp.backend.repository.UserRepository;
import com.refereeapp.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService; 

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

   @PostMapping
    public User createUser(@RequestBody User user, @RequestParam(required = false) String secretKey) {
        // Якщо користувач хоче стати адміном, перевіряємо ключ
        if ("ADMIN".equals(user.getRole())) {
            // Наш секретний ключ для захисту диплома: REFMATE-2026
            if (!"REFMATE-2026".equals(secretKey)) {
                throw new RuntimeException("Неправильний секретний ключ адміністратора!");
            }
        } else {
            // Захист: якщо роль не ADMIN, примусово ставимо REFEREE
            user.setRole("REFEREE");
        }
        return userRepository.save(user);
    }

    @GetMapping("/available")
    public List<User> getAvailableReferees(@RequestParam String date) {
        return userService.getAvailableReferees(date);
    }

    @PostMapping("/login")
    public User login(@RequestBody User loginData) {
        User user = userRepository.findByEmailAndPassword(loginData.getEmail(), loginData.getPassword());
        if (user == null) {
            throw new RuntimeException("Неправильний email або пароль");
        }
        return user;
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