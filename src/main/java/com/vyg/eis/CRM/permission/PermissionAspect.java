package com.vyg.eis.CRM.permission;

import com.vyg.eis.CRM.config.RolePermissionConfig;
import com.vyg.eis.CRM.domain.CRM.enums.Permission;
import com.vyg.eis.CRM.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint,
                                  RequiresPermission requiresPermission) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Authentication required");
        }

        for (Permission permission : requiresPermission.value()) {
            if (!RolePermissionConfig.hasPermission(principal.getRole(), permission)) {
                throw new AccessDeniedException(
                        "Insufficient permissions: requires " + permission + " for role " + principal.getRole());
            }
        }

        return joinPoint.proceed();
    }
}
