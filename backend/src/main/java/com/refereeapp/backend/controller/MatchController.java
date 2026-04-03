package com.refereeapp.backend.controller;

import com.refereeapp.backend.model.Match;
import com.refereeapp.backend.model.User;
import com.refereeapp.backend.repository.MatchRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchRepository matchRepository;

    public MatchController(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @GetMapping
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    // НОВЕ: Отримати конкретний матч по ID (для модального вікна редагування)
    @GetMapping("/{id}")
    public Match getMatchById(@PathVariable Long id) {
        return matchRepository.findById(id).orElseThrow(() -> new RuntimeException("Матч не знайдено"));
    }

    @PostMapping
    public Match createMatch(@RequestBody Match match) {
        if (match.getReferees() != null) {
            for (User ref : match.getReferees()) {
                match.getRefereeStatuses().put(ref.getId(), "PENDING");
            }
        }
        return matchRepository.save(match);
    }

    @PutMapping("/{id}/status")
    public Match updateMatchStatus(@PathVariable Long id, @RequestParam Long refereeId, @RequestParam String status) {
        return matchRepository.findById(id).map(match -> {
            match.getRefereeStatuses().put(refereeId, status);
            return matchRepository.save(match);
        }).orElseThrow(() -> new RuntimeException("Матч не знайдено"));
    }

    // НОВЕ: Повне редагування матчу
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
            match.setFinished(true); // Просто ставимо прапорець "Завершено"
            return matchRepository.save(match);
        }).orElseThrow(() -> new RuntimeException("Матч не знайдено"));
    }
}