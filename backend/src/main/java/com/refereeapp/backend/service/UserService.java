package com.refereeapp.backend.service;

import com.refereeapp.backend.model.User;
import com.refereeapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAvailableReferees(String targetDate) {
        // ЗМІНА: Тепер беремо ВСІХ користувачів з бази (і арбітрів, і адмінів)
        List<User> allUsers = userRepository.findAll();

        // Фільтруємо тих, у кого в рядку availability є потрібна дата
        return allUsers.stream()
                .filter(user -> user.getAvailability() != null && user.getAvailability().contains(targetDate))
                .collect(Collectors.toList());
    }
}