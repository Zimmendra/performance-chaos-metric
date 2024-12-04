package com.MetricInsighter.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class ContainerInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String containerName;
    private String imageName;  // Associate image with container name
    private String dockerfilePath;
    private String buildContextPath;
    private boolean isActive;
    private int deploymentCount;  // Track deployment count

    // Additional validations will be done in the service layer
}
