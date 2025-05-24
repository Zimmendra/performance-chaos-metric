/*
package com.microservice.resiliency.analyser.service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chaos")
public class ChaosController {

    @PostMapping("/start")
    public ResponseEntity<String> startChaos(@RequestParam String hostname,
                                             @RequestParam(required = false) String chaosType) {
        // chaosType could be CPU, Memory, Network etc. You can customize.

        try {
            // Call your ChaosService to trigger chaos on the hostname
            chaosService.triggerChaos(hostname, chaosType);

            return ResponseEntity.ok("Chaos started on host: " + hostname + " with chaos type: " + chaosType);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error starting chaos: " + e.getMessage());
        }

    }

    public void triggerChaos(String hostname, String chaosType) {
        switch (chaosType.toLowerCase()) {
            case "cpu":
                // Run CPU stress command on the hostname
                sshClient.execute(hostname, "stress --cpu 4 --timeout 60");
                break;
            case "memory":
                // Run memory stress
                sshClient.execute(hostname, "stress --vm 2 --vm-bytes 256M --timeout 60");
                break;
            case "network":
                // Inject network latency
                sshClient.execute(hostname, "tc qdisc add dev eth0 root netem delay 100ms");
                break;
            default:
                throw new IllegalArgumentException("Unknown chaos type: " + chaosType);
        }
    }
}*/
