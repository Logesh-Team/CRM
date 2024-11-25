package com.vyg.eis.Notification.service.Notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Service
public class CommonService {
    @Autowired
    private JwtDecoder jwtDecoder;

    public String getTenantId() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication != null && authentication.getToken() != null) {
           
            return jwtDecoder.decode(authentication.getToken().getTokenValue()).getClaim("tenant");

        }
        return null;
    }
}
