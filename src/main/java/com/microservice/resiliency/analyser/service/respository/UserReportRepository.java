package com.microservice.resiliency.analyser.service.respository;

import com.microservice.resiliency.analyser.service.model.ResiliencyScore;
import com.microservice.resiliency.analyser.service.model.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    List<UserReport> findByServiceName(String serviceName);
}
