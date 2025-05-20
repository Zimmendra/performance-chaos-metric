package com.microservice.resiliency.analyser.service.model;

import lombok.Data;

@Data
public class Metrics {

    double latencyMetrics;
    double failureRate;
    Integer noOfRequest;
}
