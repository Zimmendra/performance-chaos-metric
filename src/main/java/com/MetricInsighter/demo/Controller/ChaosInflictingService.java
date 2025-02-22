package com.MetricInsighter.demo.Controller;

import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/chaos")
public class ChaosInflictingService {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
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
        }
        return output.toString();
    }

    @PostMapping("/cpu-stress")
    public String stressCPU(@RequestParam String containerId,
                            @RequestParam(defaultValue = "4") int cores,
                            @RequestParam(defaultValue = "60") int duration) {
        return executeCommand("docker exec -it " + containerId + " stress --cpu " + cores + " --timeout " + duration + "s");
    }

    @PostMapping("/memory-stress")
    public String stressMemory(@RequestParam String containerId,
                               @RequestParam(defaultValue = "2") int vmCount,
                               @RequestParam(defaultValue = "256M") String vmBytes,
                               @RequestParam(defaultValue = "60") int duration) {
        return executeCommand("docker exec -it " + containerId + " stress --vm " + vmCount + " --vm-bytes " + vmBytes + " --timeout " + duration + "s");
    }

    @PostMapping("/disk-io")
    public String stressDiskIO(@RequestParam String containerId,
                               @RequestParam(defaultValue = "102400") int fileSizeMB) {
        return executeCommand("docker exec -it " + containerId + " sh -c \"dd if=/dev/zero of=/tmp/test bs=1M count=" + fileSizeMB + "\"");
    }

    @PostMapping("/kill-process")
    public String killProcess(@RequestParam String containerId,
                              @RequestParam(defaultValue = "java") String processName) {
        return executeCommand("docker exec -it " + containerId + " pkill -f " + processName);
    }

    @PostMapping("/network-latency")
    public String simulateNetworkLatency(@RequestParam String containerId,
                                         @RequestParam(defaultValue = "eth0") String interfaceName,
                                         @RequestParam(defaultValue = "200") int delayMs,
                                         @RequestParam(defaultValue = "10") int packetLoss) {
        return executeCommand("docker exec -it " + containerId + " tc qdisc add dev " + interfaceName + " root netem delay " + delayMs + "ms loss " + packetLoss + "%");
    }

    @PostMapping("/remove-network-latency")
    public String removeNetworkLatency(@RequestParam String containerId,
                                       @RequestParam(defaultValue = "eth0") String interfaceName) {
        return executeCommand("docker exec -it " + containerId + " tc qdisc del dev " + interfaceName + " root netem");
    }

    @PostMapping("/reduce-file-descriptors")
    public String limitFileDescriptors(@RequestParam String containerId,
                                       @RequestParam(defaultValue = "10") int limit) {
        return executeCommand("docker exec -it " + containerId + " sh -c \"ulimit -n " + limit + "\"");
    }

    @PostMapping("/restart-container")
    public String restartContainer(@RequestParam String containerId) {
        return executeCommand("docker restart " + containerId);
    }

    @PostMapping("/high-network-usage")
    public String simulateHighNetworkUsage(@RequestParam String containerId,
                                           @RequestParam(defaultValue = "8080") int port) {
        return executeCommand("docker exec -it " + containerId + " dd if=/dev/zero bs=1M | nc -l -p " + port);
    }
}