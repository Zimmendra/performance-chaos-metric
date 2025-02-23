package com.MetricInsighter.demo.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "chaos_events")
public class ChaosEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String containerId;
    private String eventType;
    private String command;
    private String status;
    private LocalDateTime timestamp;

    private LocalDateTime recoveryTime;
    private Long downtimeSeconds;

    private Double mtbf;
    private Double availability;
}
