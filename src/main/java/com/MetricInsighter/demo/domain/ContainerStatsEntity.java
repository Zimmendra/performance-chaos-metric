package com.MetricInsighter.demo.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.security.Timestamp;

@Entity
@Data
public class ContainerStatsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String containerId;
    private String name;
    private String cpuUsage;
    private String memoryUsage;
    private String memoryLimit;
    private String memoryPercentage;
    private String netIo;
    private String blockIo;
    private int pids;
    private String applicationName;
    private Timestamp timestamp;
    private int noOfDeployment;


    // Getters and setters
}
