package com.microservice.resiliency.analyser.service.controller;

import com.microservice.resiliency.analyser.service.model.MetricForSystem;
import com.microservice.resiliency.analyser.service.model.UserReport;
import com.microservice.resiliency.analyser.service.serviceLogic.MetricSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/metrics-system")
@Validated
@RequiredArgsConstructor
public class MetricSystemController {

    private final MetricSystemService metricSystemService;

    @GetMapping
    public List<MetricForSystem> getAllUserReports() {
        return metricSystemService.metricForSystemList();
    }
}
