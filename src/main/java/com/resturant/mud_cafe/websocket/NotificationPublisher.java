package com.resturant.mud_cafe.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishPaymentNotification(Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/payments", payload);
    }

    public void publishTableStatusUpdate(Long tableId, String status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tableId", tableId);
        payload.put("status", status);
        messagingTemplate.convertAndSend("/topic/tables", payload);
    }
}