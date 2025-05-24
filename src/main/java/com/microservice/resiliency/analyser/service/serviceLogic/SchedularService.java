package com.microservice.resiliency.analyser.service.serviceLogic;

import com.microservice.resiliency.analyser.service.model.ResiliencyScore;
import com.microservice.resiliency.analyser.service.model.UserReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class SchedularService {

    @Autowired
    private UserReportService userReportService;

    @Autowired
    private ResiliencyAnalyzerService resiliencyScoreService;

    @Autowired
    private EmailService emailService;


    @Scheduled(fixedRate = 60000)
    public void sendDynamicScheduledReports() {
        List<UserReport> users = userReportService.getAllUserReport();

        for (UserReport user : users) {
            String region = user.getRegion();
            ZonedDateTime preferredTime = ZonedDateTime.from(user.getLocalTime());

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(region));

            if (now.equals(preferredTime)) {
                List<ResiliencyScore> scores = resiliencyScoreService.getResiliencyScore(user.getServiceName());
                emailService.sendResiliencyReportEmail(scores, user.getEmail());
            }
        }
    }

}
