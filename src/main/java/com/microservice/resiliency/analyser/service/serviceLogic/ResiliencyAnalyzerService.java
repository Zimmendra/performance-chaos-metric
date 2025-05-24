package com.microservice.resiliency.analyser.service.serviceLogic;

import com.microservice.resiliency.analyser.service.model.Metrics;
import com.microservice.resiliency.analyser.service.model.ResiliencyScore;
import com.microservice.resiliency.analyser.service.respository.ResiliencyScoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResiliencyAnalyzerService {

    private static final Logger logger = LoggerFactory.getLogger(ResiliencyAnalyzerService.class);

    private final WebClient webClient;

    @Autowired
    public ResiliencyAnalyzerService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Autowired
    private ResiliencyScoreRepository resiliencyScoreRepository;

    @Autowired
    EmailService emailService;

    public List<ResiliencyScore> calculateAndSaveResiliency(String serviceName, String deploymentId, String serviceUrl) {
        List<ResiliencyScore> resiliencyScoreList = new ArrayList<>();

        try {
            // Fetch metrics from the service
            String metrics = webClient
                    .get()
                    .uri(serviceUrl + "/actuator/prometheus")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Calculate resiliency score without multi-threading
            ResiliencyScore score = calculateResiliency(metrics, serviceName);
            resiliencyScoreList.add(score);

        } catch (Exception e) {
            logger.error("Error calculating resiliency for service: {} with deployment: {}", serviceName, deploymentId, e);
            ResiliencyScore defaultScore = new ResiliencyScore();
            defaultScore.setServiceName(serviceName);
            defaultScore.setResiliencyScore(0.0);
            defaultScore.setFailureRate(100.0);
            defaultScore.setAvgLatency(Double.MAX_VALUE);
            defaultScore.setTimestamp(LocalDateTime.now());
            resiliencyScoreList.add(defaultScore);
        }

        return resiliencyScoreList;
    }

    private ResiliencyScore calculateResiliency(String metrics, String serviceName) {
        Metrics extractMetrics = extractMetrics(metrics);

        double resiliencyScore = computeResiliency(extractMetrics.getFailureRate(), extractMetrics.getLatencyMetrics());
        ResiliencyScore score = new ResiliencyScore();
        score.setServiceName(serviceName);
        score.setResiliencyScore(resiliencyScore);
        score.setFailureRate(extractMetrics.getFailureRate());
        score.setAvgLatency(extractMetrics.getLatencyMetrics());
        score.setTimestamp(LocalDateTime.now());
        score.setNoOfRequests(extractMetrics.getNoOfRequest());
        return score;
    }

    private Metrics extractMetrics(String metrics) {
        double totalTime = 0.0;
        int totalRequests = 0;
        double failureTime = 0.0;
        int failureRequests = 0;
        Metrics metricsDetails = new Metrics();

        String[] lines = metrics.split("\n");


        for (String line : lines) {
            String[] parts = line.split("\\s+");


            if (line.contains("http_server_requests_seconds_sum")) {
                totalTime += Double.valueOf(parts[1]);
            }


            if (line.contains("http_server_requests_seconds_count{error")) {
                if (!line.contains("/actuator/prometheus")) {
                    totalRequests += Integer.valueOf(parts[1]);
                }
            }


            if (line.contains("http_server_requests_seconds_sum{error") && line.contains("status=\"404\"")) {
                failureTime += Double.valueOf(parts[1]);
            }


            if (line.contains("http_server_requests_seconds_count{error") && line.contains("status=\"404\"")) {
                failureRequests += Integer.valueOf(parts[1]);
            }
        }

        double averageLatency = totalRequests == 0 ? 0 : totalTime / totalRequests;

        double failureRate = failureRequests == 0 ? 0 : failureTime / failureRequests;

        metricsDetails.setLatencyMetrics(averageLatency);
        metricsDetails.setFailureRate(failureRate);
        metricsDetails.setNoOfRequest(totalRequests);
        logger.info("Average Latency: " + averageLatency);
        logger.info("Failure Rate (404 errors): " + failureRate);
        return metricsDetails;
    }

    private double computeResiliency(double failureRate, double latency) {
        return 100 - (failureRate * 2) - (latency / 10);
    }

    public List<ResiliencyScore> getResiliencyScore(String serviceName) {
        return resiliencyScoreRepository.findByServiceName(serviceName);
    }

    public List<String> getServiceNames() {
        return resiliencyScoreRepository.findDistinctServiceNames();
    }

    public void saveResiliencyScore(ResiliencyScore resiliencyScore) {
        resiliencyScoreRepository.save(resiliencyScore);
    }

    public ResiliencyScore getResiliencyScoreByServiceName(String serviceName) {
        if (!resiliencyScoreRepository.findAll().isEmpty()) {
            return resiliencyScoreRepository.findAll().stream()
                    .filter(resiliencyScore -> serviceName.equals(resiliencyScore.getServiceName()))
                    .collect(Collectors.toList()).get(resiliencyScoreRepository.findAll().size() - 1);
        } else {
            return null;
        }
    }
    public void sendEmail(String serviceName,String email) {
        emailService.sendResiliencyReportEmail(resiliencyScoreRepository.findByServiceName(serviceName),email);
    }
}
