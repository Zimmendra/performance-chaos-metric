package com.microservice.resiliency.analyser.service.model.request;

import lombok.Data;

@Data
public class BuildImage {
    String imageName;
    String dockerfilePath;
    String buildContextPath;
}
