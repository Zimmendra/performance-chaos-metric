package com.microservice.resiliency.analyser.service.serviceLogic;

import com.microservice.resiliency.analyser.service.model.ResiliencyScore;
import com.microservice.resiliency.analyser.service.model.UserReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SchedularService {

    @Autowired
    private UserReportService userReportService;

    @Autowired
    private ResiliencyAnalyzerService resiliencyScoreService;

    @Autowired
    private EmailService emailService;

    @Async
    @Scheduled(fixedRate = 60000)
    public void sendDynamicScheduledReports() {
        List<UserReport> users = userReportService.getAllUserReport();

        for (UserReport user : users) {
            String region = user.getRegion();
            ZoneId zoneId = ZoneId.of(region);


            LocalDate localDate = LocalDate.now(zoneId);


            LocalTime localTime = user.getLocalTime();


            ZonedDateTime preferredTime = ZonedDateTime.of(localDate, localTime, zoneId);


            ZonedDateTime now = ZonedDateTime.now(zoneId);

            if (now.truncatedTo(ChronoUnit.MINUTES).equals(preferredTime.truncatedTo(ChronoUnit.MINUTES))) {
                List<ResiliencyScore> scores = resiliencyScoreService.getResiliencyScore(user.getServiceName());
                emailService.sendResiliencyReportEmail(scores, user.getEmail());
            }
        }
    }

}
