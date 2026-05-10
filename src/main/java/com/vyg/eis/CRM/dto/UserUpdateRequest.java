package com.vyg.eis.CRM.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String name;
    private String phone;
    private String designation;
    private String department;
    private String profilePicture;
}
