package com.refereeapp.backend.controller;

import com.refereeapp.backend.model.User;
import com.refereeapp.backend.repository.UserRepository;
import com.refereeapp.backend.service.UserService;
import com.refereeapp.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserController userController;

    @Test
    void createUser_ShouldEncryptPassword_AndSaveReferee() {
        User incomingUser = new User();
        incomingUser.setEmail("test@gmail.com");
        incomingUser.setPassword("plainTextPassword");
        incomingUser.setRole("REFEREE");

        when(passwordEncoder.encode("plainTextPassword")).thenReturn("hashedPassword123");
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userController.createUser(incomingUser, null);

        assertEquals("REFEREE", savedUser.getRole());
        
        assertEquals("hashedPassword123", savedUser.getPassword());
        
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithAdminRole_AndWrongKey_ShouldThrowException() {
        User hacker = new User();
        hacker.setRole("ADMIN");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userController.createUser(hacker, "WRONG-KEY");
        });

        assertEquals("Неправильний секретний ключ адміністратора!", exception.getMessage());
    }
}