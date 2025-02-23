package com.MetricInsighter.demo.Repository;


import com.MetricInsighter.demo.domain.ChaosEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ChaosEventRepository extends JpaRepository<ChaosEvent, Long> {

    List<ChaosEvent> findAllByContainerId(String containerId);

    ChaosEvent findTopByContainerIdOrderByTimestampDesc(String containerId);

    List<ChaosEvent> findAllByRecoveryTimeIsNotNull();

    // New method to find recovered events by container ID
    List<ChaosEvent> findAllByContainerIdAndRecoveryTimeIsNotNull(String containerId);
}


