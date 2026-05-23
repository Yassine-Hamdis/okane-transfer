package com.okanetransfer.repository;

import com.okanetransfer.entity.Notification;
import com.okanetransfer.entity.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserId(Long userId);
    List<Notification> findAllByUserIdAndIsReadFalse(Long userId);
    List<Notification> findAllByTransferId(Long transferId);
    List<Notification> findAllByChannel(NotificationChannel channel);
    long countByUserIdAndIsReadFalse(Long userId);
}