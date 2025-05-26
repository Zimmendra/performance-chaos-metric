package com.microservice.resiliency.analyser.service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "resiliency_scores")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResiliencyScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String serviceName;
    private Integer deploymentId;
    private double resiliencyScore;
    private double failureRate;
    private double avgLatency;
    private LocalDateTime timestamp;
    private Integer noOfRequests;
    private String serviceUrl;
    private Boolean errorLog;
}
