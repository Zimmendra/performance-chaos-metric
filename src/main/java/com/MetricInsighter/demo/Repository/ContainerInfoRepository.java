package com.MetricInsighter.demo.Repository;

import com.MetricInsighter.demo.domain.ContainerInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContainerInfoRepository extends JpaRepository<ContainerInfoEntity, Long> {

    List<ContainerInfoEntity> findByImageNameOrDockerfilePathOrBuildContextPath(String imageName, String dockerfilePath, String buildContextPath);

    ContainerInfoEntity findByImageName(String imageName);

    ContainerInfoEntity findByContainerName(String containerName);

    ContainerInfoEntity findByDockerfilePath(String dockerfilePath);
}
