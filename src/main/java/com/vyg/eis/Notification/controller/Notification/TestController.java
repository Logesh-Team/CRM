package com.vyg.eis.Notification.controller.Notification;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    DataSource dataSource;

    @GetMapping("/test-db")
    public String testDb() throws SQLException {
        return "Connected to: " + dataSource.getConnection().getMetaData().getURL();
    }
}