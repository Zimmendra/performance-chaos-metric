package com.microservice.resiliency.analyser.service.controller;

import com.microservice.resiliency.analyser.service.model.UserReport;
import com.microservice.resiliency.analyser.service.serviceLogic.UserReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user-report")
@CrossOrigin(origins = "http://localhost:5173/")
public class UserReportController {

    @Autowired
    private UserReportService userReportService;


    @PostMapping
    public UserReport createUserReport(@RequestBody UserReport userReport) {
        return userReportService.saveUserReport(userReport);
    }

    @PutMapping
    public UserReport updateUserReport(@RequestBody UserReport userReport) {
        return userReportService.updateUserReport(userReport);
    }


    @GetMapping
    public List<UserReport> getAllUserReports() {
        return userReportService.getAllUserReport();
    }

    @GetMapping("/service/{serviceName}")
    public List<UserReport> getByServiceName(@PathVariable String serviceName) {
        return userReportService.getAllUserReportByServiceName(serviceName);
    }

    @DeleteMapping("/delete-user-report/{id}")
    public void deleteUserReport(@PathVariable Long id){
        log.info("DeleteUserReport()");
        userReportService.deleteUserReport(id);
    }
}
