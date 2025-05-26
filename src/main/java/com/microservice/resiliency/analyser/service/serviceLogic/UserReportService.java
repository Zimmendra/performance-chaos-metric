package com.microservice.resiliency.analyser.service.serviceLogic;

import com.microservice.resiliency.analyser.service.model.UserReport;
import com.microservice.resiliency.analyser.service.respository.UserReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserReportService {

    @Autowired
    UserReportRepository userReportRepository;

    public UserReport saveUserReport(UserReport userReport) {
        return userReportRepository.save(userReport);
    }

    public UserReport updateUserReport(UserReport userReport) {
        return userReportRepository.save(userReport);
    }

    public List<UserReport> getAllUserReportByServiceName(String serviceName) {
        return userReportRepository.findByServiceName(serviceName);
    }

    public List<UserReport> getAllUserReport(){
        return userReportRepository.findAll();
    }

    public void deleteUserReport(Long id){
        userReportRepository.deleteById(id);
    }

}