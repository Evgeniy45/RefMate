package com.refereeapp.backend;

import com.refereeapp.backend.model.User;
import com.refereeapp.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync; // ДОДАНО
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableAsync // ДОДАНО: Дозволяє відправляти листи у фоновому потоці
public class BackendApplication {

    public static void main(String[] args) {
        // ДОДАНО: Примусово використовує IPv4 для уникнення таймаутів (Operation timed out) Google SMTP
        System.setProperty("java.net.preferIPv4Stack", "true"); 
        
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                
                User admin = new User();
                admin.setFullName("Ольховський Євгеній");
                admin.setEmail("jekaolhovskii@gmail.com");
                admin.setPassword(passwordEncoder.encode("0660776016")); 
                admin.setCity("Київ");
                admin.setLicenseCategory("National");
                admin.setRole("ADMIN");
                userRepository.save(admin);
                
                System.out.println("Тестових користувачів успішно створено та зашифровано!");
            } else {
                System.out.println("База вже містить дані, тестові користувачі не додавалися.");
            }
        };
    }
}