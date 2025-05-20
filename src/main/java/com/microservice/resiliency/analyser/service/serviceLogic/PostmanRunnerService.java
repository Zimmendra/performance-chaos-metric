package com.microservice.resiliency.analyser.service.serviceLogic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.resiliency.analyser.service.model.ResiliencyScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PostmanRunnerService {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ResiliencyAnalyzerService resiliencyAnalyzerService;
    @Autowired
    private ObjectMapper objectMapper;



    public String executeCollection(File postmanCollectionFile, int threads, String serviceName, String deploymentId, String serviceUrl) {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<List<ResiliencyScore>>> futures = new ArrayList<>();

        try {
            // Read the content of the provided Postman collection file
            String collectionJson = new String(Files.readAllBytes(postmanCollectionFile.toPath()));

            // Create a temporary file to store the collection JSON
            File tempFile = File.createTempFile("postman_collection", ".json");
            Files.write(tempFile.toPath(), collectionJson.getBytes());

            // Submit tasks for parallel execution
            for (int i = 0; i < threads; i++) {
                futures.add(executorService.submit(() -> runPostman(tempFile, serviceName, deploymentId, serviceUrl, threads)));
            }

            double totalLatency = 0.00;
            double totalFailureRate = 0.00;
            int totalRequests = 0;
            List<ResiliencyScore> resiliencyScores = new ArrayList<>();

            for (Future<List<ResiliencyScore>> future : futures) {
                List<ResiliencyScore> scores = future.get();
                resiliencyScores.addAll(scores);

                for (ResiliencyScore score : scores) {
                    totalLatency += score.getAvgLatency();
                    totalFailureRate += score.getFailureRate();
                    totalRequests++;
                }
            }

            if (totalRequests > 0) {
                ResiliencyScore finalScore = new ResiliencyScore();
                finalScore.setServiceName(serviceName);
                finalScore.setFailureRate(totalFailureRate / totalRequests);
                finalScore.setAvgLatency(totalLatency / totalRequests);
                finalScore.setResiliencyScore(computeResiliency(finalScore.getFailureRate(), finalScore.getAvgLatency()));
                finalScore.setTimestamp(LocalDateTime.now());
                Integer extracted = extracted(tempFile);
                int i = extracted * threads;
                log.info("Number of Request * Number of threads{}", i);
                log.info("Total Number of Requests{}", resiliencyScores.getLast().getNoOfRequests());
                Optional<ResiliencyScore> matchingScore = resiliencyScores.stream()
                        .filter(resiliencyScore -> resiliencyScore.getNoOfRequests().equals(i))
                        .findFirst();
                if(matchingScore.isPresent()){
                    finalScore.setDeploymentId(1);
                    if(resiliencyAnalyzerService.getResiliencyScoreByServiceName(serviceName)!=null){
                        finalScore.setDeploymentId(resiliencyAnalyzerService.getResiliencyScoreByServiceName(serviceName).getDeploymentId() + 1);
                    }
                }else{
                    if(resiliencyAnalyzerService.getResiliencyScoreByServiceName(serviceName)!=null) {
                        finalScore.setId(resiliencyAnalyzerService.getResiliencyScoreByServiceName(serviceName).getId());
                        finalScore.setDeploymentId(resiliencyAnalyzerService.getResiliencyScoreByServiceName(serviceName).getDeploymentId());
                    }else{
                        finalScore.setDeploymentId(1);
                    }
                }

                resiliencyAnalyzerService.saveResiliencyScore(finalScore);
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            executorService.shutdown();
            return "Error: " + e.getMessage();
        } finally {
            executorService.shutdown();
        }
        executorService.shutdown();
        return "Postman collection executed with " + threads + " threads.";

    }

    private List<ResiliencyScore> runPostman(File collectionFile, String serviceName, String deploymentId, String serviceUrl, int threads) {
        List<ResiliencyScore> resiliencyScoreList = new ArrayList<>();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\AD\\AppData\\Roaming\\npm\\newman.cmd", "run", collectionFile.getAbsolutePath()
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes());
            System.out.println(output);
            // Get calculated resiliency scores
            resiliencyScoreList = resiliencyAnalyzerService.calculateAndSaveResiliency(serviceName, deploymentId, serviceUrl);

        } catch (Exception e) {
            System.err.println("Error executing Newman: " + e.getMessage());
            e.printStackTrace();
        }

        return resiliencyScoreList;
    }


    private Integer extracted(File collectionFile) throws IOException {
        String jsonContent = new String(Files.readAllBytes(Paths.get(collectionFile.getAbsolutePath())));
        JsonNode rootNode = objectMapper.readTree(jsonContent);

        // Extract and count requests
        return (countRequests(rootNode.get("item")));
    }

    private double computeResiliency(double failureRate, double latency) {
        return 100 - (failureRate * 2) - (latency / 10);
    }

    private int countRequests(JsonNode itemsNode) {
        if (itemsNode == null || !itemsNode.isArray()) {
            return 0;
        }

        int count = 0;
        for (JsonNode item : itemsNode) {
            if (item.has("request")) {
                count++;  // Count individual requests
            } else if (item.has("item")) {
                count += countRequests(item.get("item"));  // Recursively count in nested folders
            }
        }
        return count;
    }
}
