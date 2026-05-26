package com.okanetransfer.dto.response;

import com.okanetransfer.entity.enums.NotificationChannel;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long                id;
    private String              title;
    private String              message;
    private NotificationChannel channel;
    private String              recipientAddress;
    private boolean             read;
    private LocalDateTime       readAt;
    private LocalDateTime       sentAt;
    private Long                transferId;
}