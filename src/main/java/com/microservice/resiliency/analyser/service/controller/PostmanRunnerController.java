package com.microservice.resiliency.analyser.service.controller;

import com.microservice.resiliency.analyser.service.serviceLogic.PostmanRunnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Positive;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


@RestController
@RequestMapping("/postman")
@CrossOrigin(origins = "http://localhost:5173/")
@Validated
public class PostmanRunnerController {

    @Autowired
    private PostmanRunnerService postmanRunnerService;

    @PostMapping("/run")
    public String runPostmanCollection(
            @RequestParam("threads") @Positive(message = "The threads need to be positive") int threads,
            @RequestParam("file") MultipartFile file,
            @RequestParam String serviceName,
            @RequestParam String deploymentId,
            @RequestParam String serviceUrl) {
        try {
            File tempFile = convertMultipartToFile(file);
            return postmanRunnerService.executeCollection(tempFile, threads, serviceName, deploymentId, serviceUrl);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private File convertMultipartToFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("postman_collection", ".json");
        Files.write(tempFile.toPath(), file.getBytes());
        return tempFile;
    }
}


