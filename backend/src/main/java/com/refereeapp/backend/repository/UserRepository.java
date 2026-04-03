package com.refereeapp.backend.repository;

import com.refereeapp.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRole(String role); 

    User findByEmailAndPassword(String email, String password);
}