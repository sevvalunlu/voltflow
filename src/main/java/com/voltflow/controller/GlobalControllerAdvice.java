package com.voltflow.controller;

import com.voltflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final NotificationService notificationService;

    @ModelAttribute("recentNotifications")
    public Object globalNotifications() {
        return notificationService.getRecentNotifications();
    }
}