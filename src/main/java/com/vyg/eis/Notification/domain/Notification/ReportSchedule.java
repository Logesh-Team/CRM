package com.vyg.eis.Notification.domain.Notification;
import lombok.Data;

@Data
public class ReportSchedule {
    private String reportName;
    private String frequency; // daily, weekly, date
    private String time;
    private String day;   // For weekly reports
    private String date;  // For particular date
    private String email;
}

