package com.payway.utils;

import com.payway.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUserDetails {
    public static Long getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        } else {
            throw new RuntimeException("Could not retrieve authenticated user ID");
        }
    }

    public static UserDetailsImpl getAuthenticatedUserDetails() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            return (UserDetailsImpl) principal;
        } else {
            throw new RuntimeException("Could not retrieve authenticated user details");
        }
    }
}
