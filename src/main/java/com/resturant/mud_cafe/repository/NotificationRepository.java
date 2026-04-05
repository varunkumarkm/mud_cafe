package com.resturant.mud_cafe.repository;

import com.resturant.mud_cafe.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN FETCH n.bill " +
            "WHERE n.recipient.id = :recipientId " +
            "ORDER BY n.sentAt DESC")
    List<Notification> findByRecipientIdOrderBySentAtDesc(Long recipientId);

    List<Notification> findByIsReadFalse();

    long countByRecipientIdAndIsReadFalse(Long recipientId);
}