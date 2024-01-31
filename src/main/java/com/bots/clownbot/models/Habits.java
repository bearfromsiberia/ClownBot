package com.bots.clownbot.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Habits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private Long userId;
}
