package com.microservice.resiliency.analyser.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "metrics_for_system")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricForSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    double latencyMetrics;
    double failureRateMetrics;
    String systemType;
}
