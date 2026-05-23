package com.okanetransfer.entity;

import com.okanetransfer.entity.enums.MessageIntent;
import com.okanetransfer.entity.enums.MessageSender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatbotConversation conversation;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false, length = 5)
    private MessageSender sender;

    @NotBlank
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // NULL when sender = USER (intent detected on BOT response)
    // ESCALATION intent → triggers conversation status to ESCALATED
    @Enumerated(EnumType.STRING)
    @Column(name = "intent", length = 15)
    private MessageIntent intent;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}