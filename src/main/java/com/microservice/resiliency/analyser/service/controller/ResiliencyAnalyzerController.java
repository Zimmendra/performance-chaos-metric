package com.microservice.resiliency.analyser.service.controller;

import com.microservice.resiliency.analyser.service.model.ResiliencyScore;
import com.microservice.resiliency.analyser.service.serviceLogic.ResiliencyAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resiliency")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class ResiliencyAnalyzerController {

    @Autowired
    private ResiliencyAnalyzerService resiliencyAnalyzerService;

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
    public void deleteResiliencyScore(@RequestParam Long resiliencyScoreId){
        resiliencyAnalyzerService.deleteResiliencyScore(resiliencyScoreId);
    }
}
