package com.MetricInsighter.demo.Controller;


import com.MetricInsighter.demo.dto.request.BuildImage;
import com.MetricInsighter.demo.service.DockerService;
import com.MetricInsighter.demo.service.PerformanceMetricService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/docker")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class DockerController {

    private static final Logger log = LoggerFactory.getLogger(DockerController.class);

    private final DockerService dockerService;

    private final PerformanceMetricService performanceMetricService;

    // Endpoint to build Docker image
    @PostMapping("/build")
    public ResponseEntity<?> buildImage(@RequestBody BuildImage buildImage) {
        log.info("DockerController.buildImage() invoked");
        return dockerService.runDockerBuild(buildImage);
    }

    // Endpoint to run Docker container
    @PostMapping("/run")
    public String runContainer(@RequestParam String containerName, @RequestParam String imageName, @RequestParam int port) {
        return dockerService.runDockerContainer(containerName, imageName, port);
    }

    // Endpoint to stop Docker container
    @PostMapping("/stop")
    public String stopContainer(@RequestParam String containerName) {
        return dockerService.stopDockerContainer(containerName);
    }

    // Endpoint to remove Docker container
    @PostMapping("/remove")
    public String removeContainer(@RequestParam String containerName) {
        return dockerService.removeDockerContainer(containerName);
    }

    // Endpoint to remove Docker image
    @PostMapping("/remove-image")
    public String removeImage(@RequestParam String imageName) {
        return dockerService.removeDockerImage(imageName);
    }

    @GetMapping("/performance")
    public Object getPerformance(@RequestParam String name){
        return performanceMetricService.getPerformanceMetricByApplicationName(name);
    }

    @GetMapping("/get-application-name")
    public Object getApplication(){
        return performanceMetricService.getApplication();
    }
}

