package com.vyg.eis.CRM.controller.CRM;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    @Autowired
    DataSource dataSource;

    @GetMapping("/demo")
    public String testDb() throws SQLException {
        return "Connected to: " + dataSource.getConnection().getMetaData().getURL();
    }
}