package com.voltflow.service;

import com.voltflow.entity.Notification;
import com.voltflow.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(String message, String type) {
        Notification notification = Notification.builder()
                .message(message)
                .type(type)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> getRecentNotifications() {
        return notificationRepository.findTop5ByOrderByCreatedAtDesc();
    }
}