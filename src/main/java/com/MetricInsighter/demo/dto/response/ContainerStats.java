package com.MetricInsighter.demo.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContainerStats {
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
    private int deploymentNo;
    private LocalDateTime timeStamp;

}