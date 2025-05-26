package com.microservice.resiliency.analyser.service.controller;

import com.microservice.resiliency.analyser.service.serviceLogic.PostmanRunnerService;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
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
@Validated
@RequiredArgsConstructor
public class PostmanRunnerController {


    private final PostmanRunnerService postmanRunnerService;

    @PostMapping("/run")
    public String runPostmanCollection(
            @RequestParam("threads") @Positive(message = "The threads need to be positive") @Max(value = 1000, message = "Threads must not exceed 1000")int threads,
            @RequestParam("file") MultipartFile file,
            @RequestParam String serviceName,
            @RequestParam String deploymentId,
            @RequestParam String serviceUrl) {
        try {

            return postmanRunnerService.executeCollection(file, threads, serviceName, deploymentId, serviceUrl);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }


}


