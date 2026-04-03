package com.refereeapp.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teamA;
    private String teamB;
    private String location;
    private LocalDateTime dateTime;
    private boolean finished = false; 

    @ManyToMany
    @JoinTable(
        name = "match_referees",
        joinColumns = @JoinColumn(name = "match_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> referees;

    // НОВЕ: Зберігає індивідуальні статуси (Ключ - ID судді, Значення - його Статус)
    @ElementCollection
    @CollectionTable(name = "match_referee_status", joinColumns = @JoinColumn(name = "match_id"))
    @MapKeyColumn(name = "referee_id")
    @Column(name = "status")
    private Map<Long, String> refereeStatuses = new HashMap<>();
}