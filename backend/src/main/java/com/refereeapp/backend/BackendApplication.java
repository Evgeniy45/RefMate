package com.refereeapp.backend;

import com.refereeapp.backend.model.User;
import com.refereeapp.backend.model.Match; 
import com.refereeapp.backend.repository.UserRepository;
import com.refereeapp.backend.repository.MatchRepository; 
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(UserRepository userRepository, MatchRepository matchRepository) {
		return args -> {
			// Зберігаємо користувачів і записуємо їх у змінні, щоб потім використати
			User admin = userRepository.save(new User(null, "Євгеній Ольховський", "admin@refmate.com", "123", "Київ", "National", "ADMIN", ""));
			User referee1 = userRepository.save(new User(null, "Іван Іванов", "referee@refmate.com", "123", "Львів", "First", "REFEREE", "2026-04-01, 2026-04-02"));

			// Створюємо тестовий матч і відразу призначаємо на нього нашого суддю (referee1)
			// matchRepository.save(new Match(null, "БК Київ", "БК Дніпро", "ПС Венето", LocalDateTime.of(2026, 4, 1, 18, 0), "PENDING",  java.util.List.of(referee1)));
		};
	}
}