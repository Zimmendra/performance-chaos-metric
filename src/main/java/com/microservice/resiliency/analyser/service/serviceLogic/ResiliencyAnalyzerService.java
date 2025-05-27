package com.microservice.resiliency.analyser.service.serviceLogic;

import com.microservice.resiliency.analyser.service.model.MetricForSystem;
import com.microservice.resiliency.analyser.service.model.Metrics;
import com.microservice.resiliency.analyser.service.model.ResiliencyScore;
import com.microservice.resiliency.analyser.service.respository.MetricSystemRepository;
import com.microservice.resiliency.analyser.service.respository.ResiliencyScoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microservice.resiliency.analyser.service.constant.Constants.*;

@Slf4j
@Service
public class ResiliencyAnalyzerService {


    private final WebClient webClient;

    @Autowired
    public ResiliencyAnalyzerService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Autowired
    private ResiliencyScoreRepository resiliencyScoreRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    MetricSystemRepository metricSystemRepository;

    Pattern statusPattern = Pattern.compile("status=\"(\\d{3})\"");

    public List<ResiliencyScore> calculateAndSaveResiliency(String serviceName, String deploymentId, String serviceUrl,String systemType) {
        List<ResiliencyScore> resiliencyScoreList = new ArrayList<>();

        try {
            // Fetch metrics from the service
            String metrics = webClient
                    .get()
                    .uri(serviceUrl + ACTUATOR_PROMETHEUS)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Calculate resiliency score without multi-threading
            ResiliencyScore score = calculateResiliency(metrics, serviceName,systemType);
            resiliencyScoreList.add(score);

        } catch (Exception e) {
            Throwable rootCause = e.getCause() != null ? e.getCause() : e;

            if (rootCause.getMessage() != null && rootCause.getMessage().contains(CONNECTION_REFUSED)) {
                log.error(SERVICE_AT_IS_NOT_STARTED_OR_UNREACHABLE, serviceUrl);
            } else {
                log.error(ERROR_CALCULATING_RESILIENCY_FOR_SERVICE_WITH_DEPLOYMENT, serviceName, deploymentId, e);
            }

            ResiliencyScore defaultScore = new ResiliencyScore();
            defaultScore.setServiceName(serviceName);
            defaultScore.setResiliencyScore(0.0);
            defaultScore.setFailureRate(100.0);
            defaultScore.setAvgLatency(Double.MAX_VALUE);
            defaultScore.setTimestamp(LocalDateTime.now());
            defaultScore.setErrorLog(true);
            resiliencyScoreList.add(defaultScore);
        }

        return resiliencyScoreList;
    }

    private ResiliencyScore calculateResiliency(String metrics, String serviceName,String systemType) {
        Metrics extractMetrics = extractMetrics(metrics);

        double resiliencyScore = computeResiliency(extractMetrics.getFailureRate(), extractMetrics.getLatencyMetrics(),systemType);
        ResiliencyScore score = new ResiliencyScore();
        score.setServiceName(serviceName);
        score.setResiliencyScore(resiliencyScore);
        score.setFailureRate(extractMetrics.getFailureRate());
        score.setAvgLatency(extractMetrics.getLatencyMetrics());
        score.setTimestamp(LocalDateTime.now());
        score.setNoOfRequests(extractMetrics.getNoOfRequest());
        score.setErrorLog(false);
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


            if (line.contains(HTTP_SERVER_REQUESTS_SECONDS_SUM)) {
                totalTime += Double.valueOf(parts[1]);
            }


            if (line.contains(HTTP_SERVER_REQUESTS_SECONDS_COUNT_ERROR)) {
                if (!line.contains(ACTUATOR_PROMETHEUS)) {
                    totalRequests += Integer.valueOf(parts[1]);
                }
            }


            Matcher matcher = statusPattern.matcher(line);
            if (matcher.find()) {
                int statusCode = Integer.parseInt(matcher.group(1));

                if (statusCode >= 400 && statusCode < 600) {
                    if (line.contains(HTTP_SERVER_REQUESTS_SECONDS_SUM_ERROR)) {
                        failureTime += Double.valueOf(parts[1]);
                    }

                    if (line.contains(HTTP_SERVER_REQUESTS_SECONDS_COUNT_ERROR)) {
                        failureRequests += Integer.valueOf(parts[1]);
                    }
                }
            }
        }

        double averageLatency = totalRequests == 0 ? 0 : totalTime / totalRequests;

        double failureRate = failureRequests == 0 ? 0 : failureTime / failureRequests;

        metricsDetails.setLatencyMetrics(averageLatency);
        metricsDetails.setFailureRate(failureRate);
        metricsDetails.setNoOfRequest(totalRequests);
        return metricsDetails;
    }

    private double computeResiliency(double failureRate, double latency,String systemType) {
        MetricForSystem bySystemType = metricSystemRepository.findBySystemType(systemType);
        double failureRateMetrics = bySystemType.getFailureRateMetrics();
        double latencyMetrics = bySystemType.getLatencyMetrics();
        return 100 - failureRateMetrics*(failureRate) - (latency)*latencyMetrics;
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
                    .toList().get(resiliencyScoreRepository.findAll().size() - 1);
        } else {
            return null;
        }
    }

    public void sendEmail(String serviceName,String email) {
        emailService.sendResiliencyReportEmail(resiliencyScoreRepository.findByServiceName(serviceName),email);
    }

    public void deleteResiliencyScore(Long id){
        resiliencyScoreRepository.deleteById(id);
    }
}
