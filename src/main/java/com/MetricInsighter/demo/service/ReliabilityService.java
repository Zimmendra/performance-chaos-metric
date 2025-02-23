package com.MetricInsighter.demo.service;


import com.MetricInsighter.demo.Repository.ChaosEventRepository;
import com.MetricInsighter.demo.domain.ChaosEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReliabilityService {

    @Autowired
    private ChaosEventRepository chaosEventRepository;

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
