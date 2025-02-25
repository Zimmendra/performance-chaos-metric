package com.MetricInsighter.demo.Controller;

import com.MetricInsighter.demo.service.ChaosService;
import com.MetricInsighter.demo.service.ReliabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import com.MetricInsighter.demo.domain.ChaosEvent;

@RestController
@RequestMapping("/chaos")
public class ChaosInflictingController {

    @Autowired
    private ChaosService chaosService;

    @Autowired
    private ReliabilityService reliabilityService;

    @PostMapping("/cpu-stress")
    public String stressCPU(@RequestParam String containerId,
                            @RequestParam(defaultValue = "4") int cores,
                            @RequestParam(defaultValue = "60") int duration) {
        String command = "docker exec -it " + containerId + " stress --cpu " + cores + " --timeout " + duration + "s";
        String response = chaosService.executeChaosCommand(containerId, "CPU_STRESS", command);

        System.out.println("Executed command: " + command);
        System.out.println("Chaos Service Response: " + response);

        return response;
    }
    @PostMapping("/memory-stress")
    public String stressMemory(@RequestParam String containerId,
                               @RequestParam(defaultValue = "2") int vmCount,
                               @RequestParam(defaultValue = "256M") String vmBytes,
                               @RequestParam(defaultValue = "60") int duration) {
        String command = "docker exec -it " + containerId + " stress --vm " + vmCount + " --vm-bytes " + vmBytes + " --timeout " + duration + "s";
        return chaosService.executeChaosCommand(containerId, "MEMORY_STRESS", command);
    }
    @PostMapping("/memory-limiter")
    public String limitMemory(@RequestParam String containerId,
                              @RequestParam String imageName,
                              @RequestParam(defaultValue = "100M") String memoryLimit) {
        // Stop the container
        String stopCommand = "docker stop " + containerId;
        chaosService.executeChaosCommand(containerId, "STOP_CONTAINER", stopCommand);

        // Get the current container settings (for example, environment variables, volumes, etc.)
        // Note: You will need to implement logic to retrieve these settings, as this example is simplified
        String currentConfig = chaosService.getCurrentContainerConfig(containerId);

        // Recreate and start the container with the specified memory limit
        String startCommand = "docker run --memory=" + memoryLimit + " --name " + containerId + " " + currentConfig + " " + imageName;
        String response = chaosService.executeChaosCommand(containerId, "RESTART_CONTAINER", startCommand);

        return response;
    }


    @PostMapping("/disk-io")
    public String stressDiskIO(@RequestParam String containerId,
                               @RequestParam(defaultValue = "102400") int fileSizeMB) {
        String command = "docker exec -it " + containerId + " sh -c \"dd if=/dev/zero of=/tmp/test bs=1M count=" + fileSizeMB + "\"";
        return chaosService.executeChaosCommand(containerId, "DISK_IO", command);
    }

    @PostMapping("/kill-process")
    public String killProcess(@RequestParam String containerId,
                              @RequestParam(defaultValue = "java") String processName) {
        String command = "docker exec -it " + containerId + " pkill -f " + processName;
        return chaosService.executeChaosCommand(containerId, "KILL_PROCESS", command);
    }

    @PostMapping("/network-latency")
    public String simulateNetworkLatency(@RequestParam String containerId,
                                         @RequestParam(defaultValue = "eth0") String interfaceName,
                                         @RequestParam(defaultValue = "200") int delayMs,
                                         @RequestParam(defaultValue = "10") int packetLoss) {
        String command = "docker exec -it " + containerId + " tc qdisc add dev " + interfaceName + " root netem delay " + delayMs + "ms loss " + packetLoss + "%";
        return chaosService.executeChaosCommand(containerId, "NETWORK_LATENCY", command);
    }

    @PostMapping("/remove-network-latency")
    public String removeNetworkLatency(@RequestParam String containerId,
                                       @RequestParam(defaultValue = "eth0") String interfaceName) {
        String command = "docker exec -it " + containerId + " tc qdisc del dev " + interfaceName + " root netem";
        return chaosService.executeChaosCommand(containerId, "REMOVE_NETWORK_LATENCY", command);
    }

    @PostMapping("/restart-container")
    public String restartContainer(@RequestParam String containerId) {
        String result = chaosService.restartContainer(containerId);
        reliabilityService.calculateAndSetMTBF(containerId); // Calculate MTBF after restart
        reliabilityService.calculateAndSetAvailability(containerId); // Calculate Availability after restart
        return  result;
    }

    @GetMapping("/mttr")
    public ResponseEntity<?> getMTTR(@RequestParam String containerId) {
        double mttr = chaosService.calculateMTTR(containerId);
        return ResponseEntity.ok(Collections.singletonMap("MTTR (seconds)", mttr));
    }

    @GetMapping("/mtbf")
    public ResponseEntity<?> getMTBF(@RequestParam String containerId) {
        reliabilityService.calculateAndSetMTBF(containerId);
        List<ChaosEvent> allEvents = chaosService.findAllByContainerId(containerId);
        if (!allEvents.isEmpty()) {
            double mtbf = allEvents.get(0).getMtbf(); // Assuming MTBF is the same for all events of the same container
            return ResponseEntity.ok(Collections.singletonMap("MTBF (seconds)", mtbf));
        } else {
            return ResponseEntity.ok(Collections.singletonMap("MTBF (seconds)", 0.0)); // Or handle the case when there are no events
        }
    }

    @GetMapping("/availability")
    public ResponseEntity<?> getAvailability(@RequestParam String containerId) {
        reliabilityService.calculateAndSetAvailability(containerId);
        List<ChaosEvent> allEvents = chaosService.findAllByContainerId(containerId);
        if (!allEvents.isEmpty()) {
            double availability = allEvents.get(0).getAvailability(); // Assuming Availability is the same for all events of the same container
            return ResponseEntity.ok(Collections.singletonMap("Availability (%)", availability));
        } else {
            return ResponseEntity.ok(Collections.singletonMap("Availability (%)", 0.0)); // Or handle the case when there are no events
        }
    }
}
