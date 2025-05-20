package com.microservice.resiliency.analyser.service.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.microservice.resiliency.analyser.service.model.ResiliencyScore;

@Repository
public interface ResiliencyScoreRepository extends JpaRepository<ResiliencyScore, Long> {
}
