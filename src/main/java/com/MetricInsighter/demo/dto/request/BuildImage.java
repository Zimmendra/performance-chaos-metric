package com.MetricInsighter.demo.dto.request;

import lombok.Data;

@Data
public class BuildImage {
    String imageName;
    String dockerfilePath;
    String buildContextPath;
}
