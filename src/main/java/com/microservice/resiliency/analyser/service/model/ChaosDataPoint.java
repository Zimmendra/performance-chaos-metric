package com.microservice.resiliency.analyser.service.model;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ChaosDataPoint {
    public int deploymentId;
    public double resiliencyScore;
    public double failureRate;
    public double avgLatency;
    public LocalDateTime timestamp;

    public ChaosDataPoint(int deploymentId, double resiliencyScore, double failureRate, double avgLatency, LocalDateTime timestamp) {
        this.deploymentId = deploymentId;
        this.resiliencyScore = resiliencyScore;
        this.failureRate = failureRate;
        this.avgLatency = avgLatency;
        this.timestamp = timestamp;
    }
}