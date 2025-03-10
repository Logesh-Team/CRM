package com.vyg.eis.Notification.service.Notification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.vyg.eis.Notification.domain.Notification.ReportSchedule;

@Service
public class ReportScheduleService {
    private List<ReportSchedule> reportSchedules;

    public ReportScheduleService() {
        loadJsonFile();
    }

    private void loadJsonFile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            reportSchedules = objectMapper.readValue(
                    new File("/opt/eVyoog/ticket_sandbox/scheduledReport.json"),
                    new TypeReference<List<ReportSchedule>>() {
                    });
            System.out.println("JSON File Loaded Successfully!");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Scheduled(cron = "0 * * * * *") // Every minute
    public void scheduleReports() {
        System.out.println("Scheduler Running...");

        LocalTime currentTime = LocalTime.now();
        LocalDate currentDate = LocalDate.now();
System.err.println(reportSchedules);
        for (ReportSchedule report : reportSchedules) {
            String currentTimeFormatted = currentTime.toString().substring(0, 5);
System.out.println("Current Time: " + currentTimeFormatted);
System.out.println("Report Time: " + report.getTime());

if (report.getTime().equals(currentTimeFormatted)) {
    System.out.println("Matched Report Time: " + report.getReportName());
    switch (report.getFrequency()) {
        case "daily":
            sendReport(report);
            break;

        case "weekly":
            if (currentDate.getDayOfWeek().name().equalsIgnoreCase(report.getDay())) {
                sendReport(report);
            }
            break;

        case "date":
            System.out.println("DATE");
            if (currentDate.toString().equals(report.getDate())) {
                System.out.println("IF");
                sendReport(report);
            }
            break;
    }
}

        }
    }

    private void sendReport(ReportSchedule report) {
        System.out.println("Sending Report: " + report.getReportName() + " to " + report.getEmail());
        // Write your mail sending logic here
    }
}
