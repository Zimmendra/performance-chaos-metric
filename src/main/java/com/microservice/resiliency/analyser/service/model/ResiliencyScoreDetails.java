package com.microservice.resiliency.analyser.service.model;

import lombok.Data;

import java.util.List;

@Data
public class ResiliencyScoreDetails {

    Integer totalRequest;

    List<ResiliencyScore> resiliencyScoreList;
}
