package com.microservice.resiliency.analyser.service.serviceLogic;

import com.microservice.resiliency.analyser.service.model.MetricForSystem;
import com.microservice.resiliency.analyser.service.respository.MetricSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricSystemService {

    private final MetricSystemRepository metricSystemRepository;

    public List<MetricForSystem> metricForSystemList(){
        return metricSystemRepository.findAll();
    }

}
