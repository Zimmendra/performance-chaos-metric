package com.microservice.resiliency.analyser.service.controller;

import com.microservice.resiliency.analyser.service.model.ResiliencyScore;
import com.microservice.resiliency.analyser.service.serviceLogic.ResiliencyAnalyzerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resiliency")
@RequiredArgsConstructor
public class ResiliencyAnalyzerController {


    private final ResiliencyAnalyzerService resiliencyAnalyzerService;

    @GetMapping("/score")
    public List<ResiliencyScore> getResiliencyScore(@RequestParam String serviceName) {
        return resiliencyAnalyzerService.getResiliencyScore(serviceName);
    }

    @GetMapping("/get-service-name")
    public List<String> getServiceNames(){
        return resiliencyAnalyzerService.getServiceNames();
    }

    @GetMapping("/service-start")
    public String serviceStart(){
        return "service-start";
    }

    @GetMapping("/send-email")
    public void sendEmail(@RequestParam String serviceName,@RequestParam String email){
        resiliencyAnalyzerService.sendEmail(serviceName,email);
    }

    @DeleteMapping("/delete-resiliency-score")
    public void deleteResiliencyScore(@RequestParam Long id){
        resiliencyAnalyzerService.deleteResiliencyScore(id);
    }

}
