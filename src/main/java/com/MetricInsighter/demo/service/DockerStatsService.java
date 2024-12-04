package com.MetricInsighter.demo.service;

import com.MetricInsighter.demo.Repository.ContainerInfoRepository;
import com.MetricInsighter.demo.Repository.ContainerStatsRepository;
import com.MetricInsighter.demo.domain.ContainerStatsEntity;
import com.MetricInsighter.demo.dto.response.ContainerStats;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerStatsService {

    private final ContainerStatsRepository containerStatsRepository;

    private final ContainerInfoRepository containerInfoRepository;


    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void collectDockerStats() {
        log.info("collectDockerStats() invoked");
        List<ContainerStats> stats = getDockerStats();
        // Save stats to the database
        saveStatsToDatabase(stats);
    }

    public List<ContainerStats> getDockerStats() {
        List<ContainerStats> statsList = new ArrayList<>();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker", "ps", "--format", "{{.ID}} {{.Names}}");
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                String containerId = parts[0];
                String containerName = parts[1];
                String applicationName = extractApplicationName(containerName); // Implement this method to map container names to application names

                ContainerStats stats = getDockerStatsForContainer(containerId);
                stats.setApplicationName(applicationName);
                statsList.add(stats);
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statsList;
    }

    private ContainerStats getDockerStatsForContainer(String containerId) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("docker", "stats", "--no-stream", containerId);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<String> rawStats = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            rawStats.add(line);
        }

        process.waitFor();
        return parseDockerStats(rawStats).get(0); // There's only one container stat in the result
    }

    private List<ContainerStats> parseDockerStats(List<String> rawStats) {
        List<ContainerStats> statsList = new ArrayList<>();
        for (int i = 1; i < rawStats.size(); i++) {
            String[] columns = rawStats.get(i).split("\\s+");
            ContainerStats stats = new ContainerStats();
            stats.setContainerId(columns[0]);
            stats.setName(columns[1]);
            stats.setCpuUsage(columns[2]);
            stats.setMemoryUsage(columns[3]);
            stats.setMemoryLimit(columns[5]);
            stats.setMemoryPercentage(columns[6]);
            stats.setNetIo(columns[7] + " " + columns[8]);
            stats.setBlockIo(columns[9] + " " + columns[10]);
            stats.setPids(Integer.parseInt(columns[13]));
            statsList.add(stats);
        }
        return statsList;
    }

    private void saveStatsToDatabase(List<ContainerStats> stats) {
        for (ContainerStats stat : stats) {
            ContainerStatsEntity entity = new ContainerStatsEntity();
            entity.setContainerId(stat.getContainerId());
            entity.setName(stat.getName());
            entity.setCpuUsage(stat.getCpuUsage());
            entity.setMemoryUsage(stat.getMemoryUsage());
            entity.setMemoryLimit(stat.getMemoryLimit());
            entity.setMemoryPercentage(stat.getMemoryPercentage());
            entity.setNetIo(stat.getNetIo());
            entity.setBlockIo(stat.getBlockIo());
            entity.setPids(stat.getPids());
            entity.setApplicationName(stat.getApplicationName());
            entity.setNoOfDeployment(containerInfoRepository.findByContainerName(stat.getApplicationName()).getDeploymentCount());
            containerStatsRepository.save(entity);
        }
    }

    private String extractApplicationName(String containerName) {
        // Implement your logic to map container names to application names
        // For example, you could use a naming convention or a lookup table
        return containerName.split("_")[0]; // Example: if container name is "app1_container", it returns "app1"
    }
}
