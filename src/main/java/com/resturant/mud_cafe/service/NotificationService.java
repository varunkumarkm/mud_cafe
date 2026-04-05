package com.resturant.mud_cafe.service;

import com.resturant.mud_cafe.dto.response.NotificationResponse;
import com.resturant.mud_cafe.exception.ResourceNotFoundException;
import com.resturant.mud_cafe.repository.NotificationRepository;
import com.resturant.mud_cafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository
                .findByRecipientIdOrderBySentAtDesc(user.getId())
                .stream()
                .map(n -> {
                    Long billId = null;
                    try {
                        if (n.getBill() != null) {
                            billId = n.getBill().getId();
                        }
                    } catch (Exception ignored) {}

                    return NotificationResponse.builder()
                            .id(n.getId())
                            .message(n.getMessage())
                            .type(n.getType())
                            .read(n.isRead())
                            .sentAt(n.getSentAt())
                            .billId(billId)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        var notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        var saved = notificationRepository.save(notification);

        Long billId = null;
        try {
            if (saved.getBill() != null) {
                billId = saved.getBill().getId();
            }
        } catch (Exception ignored) {}

        return NotificationResponse.builder()
                .id(saved.getId())
                .message(saved.getMessage())
                .type(saved.getType())
                .read(saved.isRead())
                .sentAt(saved.getSentAt())
                .billId(billId)
                .build();
    }

    @Transactional
    public void markAllRead() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var notifications = notificationRepository
                .findByRecipientIdOrderBySentAtDesc(user.getId());
        notifications.forEach(n -> {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(notifications);
    }
}