package com.refereeapp.backend.controller;

import com.refereeapp.backend.model.Match;
import com.refereeapp.backend.model.User;
import com.refereeapp.backend.repository.MatchRepository;
import com.refereeapp.backend.repository.UserRepository;
import com.refereeapp.backend.service.EmailService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchRepository matchRepository;
    private final EmailService emailService;     
    private final UserRepository userRepository; 

    public MatchController(MatchRepository matchRepository, EmailService emailService, UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    @GetMapping("/{id}")
    public Match getMatchById(@PathVariable Long id) {
        return matchRepository.findById(id).orElseThrow(() -> new RuntimeException("Матч не знайдено"));
    }

    @PostMapping
    public Match createMatch(@RequestBody Match match) {
        // 1. Встановлюємо статус PENDING для всіх призначених арбітрів
        if (match.getReferees() != null) {
            for (User ref : match.getReferees()) {
                match.getRefereeStatuses().put(ref.getId(), "PENDING");
            }
        }
        
        // 2. Зберігаємо матч у базу
        Match savedMatch = matchRepository.save(match);

        // 3. Розсилаємо листи всім призначеним суддям
        if (savedMatch.getReferees() != null) {
            for (User ref : savedMatch.getReferees()) {
                // Дістаємо повну інформацію про суддю з бази (щоб отримати його email)
                userRepository.findById(ref.getId()).ifPresent(fullReferee -> {
                    emailService.sendMatchAssignmentEmail(
                            fullReferee.getEmail(),
                            fullReferee.getFullName(),
                            savedMatch.getTeamA(),
                            savedMatch.getTeamB(),
                            savedMatch.getDateTime().toString(),
                            savedMatch.getLocation()
                    );
                });
            }
        }

        return savedMatch;
    }

    @PutMapping("/{id}/status")
    public Match updateMatchStatus(@PathVariable Long id, @RequestParam Long refereeId, @RequestParam String status) {
        return matchRepository.findById(id).map(match -> {
            match.getRefereeStatuses().put(refereeId, status);
            return matchRepository.save(match);
        }).orElseThrow(() -> new RuntimeException("Матч не знайдено"));
    }

    @PutMapping("/{id}")
    public Match updateMatch(@PathVariable Long id, @RequestBody Match updatedMatch) {
        return matchRepository.findById(id).map(match -> {
            match.setTeamA(updatedMatch.getTeamA());
            match.setTeamB(updatedMatch.getTeamB());
            match.setLocation(updatedMatch.getLocation());
            match.setDateTime(updatedMatch.getDateTime());
            
            // Оновлюємо бригаду суддів
            match.setReferees(updatedMatch.getReferees());
            
            // Зберігаємо статуси старих суддів, а новим ставимо PENDING
            Map<Long, String> currentStatuses = match.getRefereeStatuses();
            Map<Long, String> newStatuses = new HashMap<>();
            
            if (updatedMatch.getReferees() != null) {
                for (User ref : updatedMatch.getReferees()) {
                    newStatuses.put(ref.getId(), currentStatuses.getOrDefault(ref.getId(), "PENDING"));
                }
            }
            match.setRefereeStatuses(newStatuses);
            
            return matchRepository.save(match);
        }).orElseThrow(() -> new RuntimeException("Матч не знайдено"));
    }

    @DeleteMapping("/{id}")
    public void deleteMatch(@PathVariable Long id) {
        matchRepository.deleteById(id);
    }

    @PutMapping("/{id}/finish")
    public Match finishMatch(@PathVariable Long id) {
        return matchRepository.findById(id).map(match -> {
            match.setFinished(true);
            return matchRepository.save(match);
        }).orElseThrow(() -> new RuntimeException("Матч не знайдено"));
    }
}