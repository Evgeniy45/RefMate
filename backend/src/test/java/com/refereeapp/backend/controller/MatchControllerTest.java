package com.refereeapp.backend.controller;

import com.refereeapp.backend.model.Match;
import com.refereeapp.backend.model.User;
import com.refereeapp.backend.repository.MatchRepository;
import com.refereeapp.backend.repository.UserRepository;
import com.refereeapp.backend.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchControllerTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private MatchController matchController;

    @Test
    void createMatch_ShouldSetPendingStatus_AndSendEmails() {
        
        User referee = new User();
        referee.setId(1L);
        referee.setFullName("Іван Суддя");
        referee.setEmail("ivan@test.com");

        Match incomingMatch = new Match();
        incomingMatch.setTeamA("Київ");
        incomingMatch.setTeamB("Львів");
        incomingMatch.setDateTime(LocalDateTime.now());
        incomingMatch.setLocation("Арена");
        incomingMatch.setReferees(List.of(referee));
        incomingMatch.setRefereeStatuses(new HashMap<>()); 

        when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(referee));

        Match savedMatch = matchController.createMatch(incomingMatch);

        
        assertEquals("PENDING", savedMatch.getRefereeStatuses().get(1L));
        
        verify(emailService, times(1)).sendMatchAssignmentEmail(
                eq("ivan@test.com"), 
                eq("Іван Суддя"), 
                eq("Київ"), 
                eq("Львів"), 
                anyString(), 
                eq("Арена")
        );
    }

    @Test
    void updateMatchStatus_ShouldChangeRefereeStatusToAccepted() {
        
        Match existingMatch = new Match();
        existingMatch.setId(100L);
        Map<Long, String> statuses = new HashMap<>();
        statuses.put(1L, "PENDING"); 
        existingMatch.setRefereeStatuses(statuses);

        when(matchRepository.findById(100L)).thenReturn(Optional.of(existingMatch));
        when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

        Match updatedMatch = matchController.updateMatchStatus(100L, 1L, "ACCEPTED");

        
        assertEquals("ACCEPTED", updatedMatch.getRefereeStatuses().get(1L));
    }
}