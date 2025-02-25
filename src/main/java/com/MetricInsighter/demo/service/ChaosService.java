package com.MetricInsighter.demo.service;

import com.MetricInsighter.demo.Repository.ChaosEventRepository;
import com.MetricInsighter.demo.domain.ChaosEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChaosService {

    private static final Logger logger = LoggerFactory.getLogger(ChaosService.class);

    @Autowired
    private ChaosEventRepository chaosEventRepository;

    public String getCurrentContainerConfig(String containerId) {
        StringBuilder config = new StringBuilder();
        logger.info("Fetching configuration for container ID: {}", containerId);
        try {
            // Execute the Docker inspect command
            String command = "docker inspect " + containerId;
            logger.debug("Executing command: {}", command);
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                config.append(line).append("\n");
            }

            process.waitFor();
            logger.info("Successfully retrieved configuration for container ID: {}", containerId);
        } catch (Exception e) {
            logger.error("Error fetching configuration for container ID: {}: {}", containerId, e.getMessage());
            return ""; // Return empty string in case of error
        }

        return config.toString(); // Return the JSON configuration as a string
    }

    public String executeChaosCommand(String containerId, String eventType, String command) {

        StringBuilder output = new StringBuilder();
        String status = "SUCCESS";

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
        } catch (Exception e) {
            output.append("Error: ").append(e.getMessage());
            status = "FAILED";
        }

        // Save the event to the database
        ChaosEvent event = new ChaosEvent();
        event.setContainerId(containerId);
        event.setEventType(eventType);
        event.setCommand(command);
        event.setStatus(status);
        event.setTimestamp(LocalDateTime.now());

        chaosEventRepository.save(event);

        return output.toString();
    }
    public double calculateMTTR(String containerId) {
        // Fetch all recovered events for the specified container ID
        List<ChaosEvent> recoveredEvents = chaosEventRepository.findAllByContainerIdAndRecoveryTimeIsNotNull(containerId);

        if (recoveredEvents.isEmpty()) {
            return 0.0; // No failures recovered yet for this container
        }

        double totalDowntime = recoveredEvents.stream()
                .mapToDouble(ChaosEvent::getDowntimeSeconds)
                .sum();

        return totalDowntime / recoveredEvents.size();
    }

    public String restartContainer(String containerId) {
        LocalDateTime recoveryTime = LocalDateTime.now();
        String command = "docker restart " + containerId;
        String result = executeChaosCommand(containerId, "RESTART_CONTAINER", command);

        // Find the latest failure event for this container
        ChaosEvent latestEvent = chaosEventRepository.findTopByContainerIdOrderByTimestampDesc(containerId);

        if (latestEvent != null) {
            Duration downtime = Duration.between(latestEvent.getTimestamp(), recoveryTime);
            latestEvent.setRecoveryTime(recoveryTime);
            latestEvent.setDowntimeSeconds(downtime.getSeconds());
            chaosEventRepository.save(latestEvent);
        }

        return result;
    }

    public List<ChaosEvent> findAllByContainerId(String containerId){
        return chaosEventRepository.findAllByContainerId(containerId);
    }
    public void calculateAndSetMTBF(String containerId) {
        List<ChaosEvent> allEvents = chaosEventRepository.findAllByContainerId(containerId);
        if (allEvents.size() < 2) {
            return; // Need at least two events to calculate MTBF
        }

        // Assuming events are ordered by timestamp
        LocalDateTime firstEventTime = allEvents.get(0).getTimestamp();
        LocalDateTime lastEventTime = allEvents.get(allEvents.size() - 1).getTimestamp();

        Duration totalOperatingTime = Duration.between(firstEventTime, lastEventTime);
        long numberOfFailures = allEvents.stream().filter(event -> event.getRecoveryTime() != null).count();

        if (numberOfFailures > 0) {
            double mtbf = (double) totalOperatingTime.getSeconds() / numberOfFailures;

            // Set MTBF for each event
            for (ChaosEvent event : allEvents) {
                event.setMtbf(mtbf);
                chaosEventRepository.save(event);
            }
        }
    }

    public void calculateAndSetAvailability(String containerId) {
        List<ChaosEvent> allEvents = chaosEventRepository.findAllByContainerId(containerId);
        if (allEvents.isEmpty()) {
            return; // No events to calculate availability
        }

        LocalDateTime firstEventTime = allEvents.get(0).getTimestamp();
        LocalDateTime lastEventTime = LocalDateTime.now();  // Or the time of the last event

        Duration totalTime = Duration.between(firstEventTime, lastEventTime);
        long totalDowntimeSeconds = allEvents.stream()
                .filter(event -> event.getDowntimeSeconds() != null)
                .mapToLong(ChaosEvent::getDowntimeSeconds)
                .sum();

        double uptimeSeconds = totalTime.getSeconds() - totalDowntimeSeconds;

        double availability = (uptimeSeconds / totalTime.getSeconds()) * 100;

        // Set Availability for each event
        for (ChaosEvent event : allEvents) {
            event.setAvailability(availability);
            chaosEventRepository.save(event);
        }
    }


}
