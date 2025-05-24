package com.microservice.resiliency.analyser.service.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.microservice.resiliency.analyser.service.model.ResiliencyScore;

import java.util.List;

@Repository
public interface ResiliencyScoreRepository extends JpaRepository<ResiliencyScore, Long> {

    List<ResiliencyScore> findByServiceName(String serviceName);

    @Query("SELECT DISTINCT r.serviceName FROM ResiliencyScore r")
    List<String> findDistinctServiceNames();
}