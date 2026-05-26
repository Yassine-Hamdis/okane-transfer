package com.okanetransfer.service;

import com.okanetransfer.dto.response.NotificationResponse;
import com.okanetransfer.entity.Notification;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.NotificationChannel;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.infrastructure.brevo.BrevoEmailSender;
import com.okanetransfer.infrastructure.brevo.BrevoSmsSender;
import com.okanetransfer.repository.NotificationRepository;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private BrevoEmailSender brevoEmailSender;

    @Autowired
    private BrevoSmsSender brevoSmsSender;

    // ─────────────────────────────────────────────────────
    //  SEND METHODS — called by other services
    // ─────────────────────────────────────────────────────

    /**
     * Main send method — called by TransferService, AuthService, etc.
     *
     * @param userId           recipient user ID
     * @param transferId       related transfer ID (null if not transfer-related)
     * @param title            notification title
     * @param message          notification body
     * @param channel          EMAIL / SMS / PUSH
     * @param recipientAddress the actual email or phone used at send time
     */
    @Transactional
    public void send(Long userId,
                     Long transferId,
                     String title,
                     String message,
                     NotificationChannel channel,
                     String recipientAddress) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            Transfer transfer = null;
            if (transferId != null) {
                transfer = transferRepository.findById(transferId).orElse(null);
            }

            Notification notification = Notification.builder()
                    .user(user)
                    .transfer(transfer)
                    .title(title)
                    .message(message)
                    .channel(channel)
                    .recipientAddress(recipientAddress)
                    .read(false)
                    .build();

            notificationRepository.save(notification);

            // real sending
            switch (channel) {
                case EMAIL -> brevoEmailSender.sendEmail(
                        recipientAddress,
                        user.getFirstName() + " " + user.getLastName(),
                        title,
                        message
                );
                case SMS -> brevoSmsSender.sendSms(recipientAddress, message);
                case PUSH -> log.info("[PUSH] to={} title='{}' (not implemented yet)",
                        recipientAddress, title);
            }

            // logging
            log.info("[NOTIFICATION] channel={} to={} title='{}'",
                    channel, recipientAddress, title);

        } catch (Exception e) {
            // Notifications must NEVER break the main flow
            log.error("Failed to send notification to userId={}: {}",
                    userId, e.getMessage());
        }
    }

    /**
     * Shortcut — send without a specific transfer context.
     * Used for: credentials delivery, password reset, account alerts
     */
    @Transactional
    public void send(Long userId,
                     String title,
                     String message,
                     NotificationChannel channel,
                     String recipientAddress) {
        send(userId, null, title, message, channel, recipientAddress);
    }

    /**
     * Send client credentials after auto-account creation.
     * Called by TransferService when a new ROLE_CLIENT account is created.
     */
    @Transactional
    public void sendCredentials(Long userId,
                                String email,
                                String phone,
                                String rawPassword) {
        String title = "Welcome to OkaneTransfer — Your Account is Ready";
        String message = String.format(
                "Hello,\n\n" +
                        "An account has been created for you on OkaneTransfer.\n\n" +
                        "Your login credentials:\n" +
                        "  Email:    %s\n" +
                        "  Password: %s\n\n" +
                        "Please log in and change your password immediately.\n" +
                        "Visit: http://localhost:4200/login\n\n" +
                        "OkaneTransfer Team",
                email, rawPassword
        );

        // Send by email if available
        send(userId, title, message, NotificationChannel.EMAIL, email);

        // Also send by SMS as backup
        String smsMessage = String.format(
                "OkaneTransfer: Account created. Email: %s | Password: %s | " +
                        "Login at okanetransfer.com and change your password.",
                email, rawPassword
        );
        send(userId, title, smsMessage, NotificationChannel.SMS, phone);
    }

    /**
     * Notify sender that their transfer was created successfully.
     */
    @Transactional
    public void notifyTransferCreated(Long userId,
                                      Long transferId,
                                      String withdrawalCode,
                                      String recipientName,
                                      String amount,
                                      String currency,
                                      String recipientAddress) {
        String title = "Transfer Sent Successfully";
        String message = String.format(
                "Your transfer of %s %s to %s has been sent.\n" +
                        "Withdrawal Code: %s\n" +
                        "Share this code with the recipient to collect the funds.",
                amount, currency, recipientName, withdrawalCode
        );
        send(userId, transferId, title, message,
                NotificationChannel.EMAIL, recipientAddress);
    }

    /**
     * Notify recipient that money is available for pickup.
     */
    @Transactional
    public void notifyTransferAvailable(Long userId,
                                        Long transferId,
                                        String amount,
                                        String currency,
                                        String agencyName,
                                        String recipientAddress) {
        String title = "Money Available for Pickup";
        String message = String.format(
                "You have %s %s waiting for you at %s.\n" +
                        "Please bring a valid ID to collect your funds.",
                amount, currency, agencyName
        );
        send(userId, transferId, title, message,
                NotificationChannel.SMS, recipientAddress);
    }

    /**
     * Notify that a transfer was paid out.
     */
    @Transactional
    public void notifyTransferPaid(Long userId,
                                   Long transferId,
                                   String amount,
                                   String currency,
                                   String recipientAddress) {
        String title = "Transfer Paid Out";
        String message = String.format(
                "Your transfer of %s %s has been successfully paid out to the recipient.",
                amount, currency
        );
        send(userId, transferId, title, message,
                NotificationChannel.EMAIL, recipientAddress);
    }

    /**
     * Notify that a transfer was cancelled.
     */
    @Transactional
    public void notifyTransferCancelled(Long userId,
                                        Long transferId,
                                        String reason,
                                        String recipientAddress) {
        String title = "Transfer Cancelled";
        String message = String.format(
                "Your transfer has been cancelled.\nReason: %s\n" +
                        "Please contact your agency for more information.",
                reason
        );
        send(userId, transferId, title, message,
                NotificationChannel.EMAIL, recipientAddress);
    }

    // ─────────────────────────────────────────────────────
    //  QUERY METHODS — used by NotificationController
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository.findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnread(Long userId) {
        return notificationRepository.findAllByUserIdAndReadFalse(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification", notificationId));

        // Security: ensure user can only mark their own notifications
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied to this notification");
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread =
                notificationRepository.findAllByUserIdAndReadFalse(userId);
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(n -> {
            n.setRead(true);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
    }

    // ─────────────────────────────────────────────────────
    //  MAPPER
    // ─────────────────────────────────────────────────────

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .channel(n.getChannel())
                .recipientAddress(n.getRecipientAddress())
                .read(n.isRead())
                .readAt(n.getReadAt())
                .sentAt(n.getSentAt())
                .transferId(n.getTransfer() != null ? n.getTransfer().getId() : null)
                .build();
    }
}