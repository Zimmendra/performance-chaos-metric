package com.MetricInsighter.demo.Repository;

import com.MetricInsighter.demo.domain.ContainerStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContainerStatsRepository extends JpaRepository<ContainerStatsEntity, Long> {

    List<ContainerStatsEntity> findAllByNameOrderByNameAscIdAsc(String name);
}