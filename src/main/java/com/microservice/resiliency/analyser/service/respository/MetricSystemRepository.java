package com.microservice.resiliency.analyser.service.respository;

import com.microservice.resiliency.analyser.service.model.MetricForSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface  MetricSystemRepository extends JpaRepository<MetricForSystem, Long> {

    MetricForSystem findBySystemType(String systemType);
}
