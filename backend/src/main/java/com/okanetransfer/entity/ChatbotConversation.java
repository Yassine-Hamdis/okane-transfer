package com.okanetransfer.entity;

import com.okanetransfer.entity.enums.ChatLanguage;
import com.okanetransfer.entity.enums.ConversationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NULL for anonymous (not logged in) users
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // Random UUID per browser session — must be unique
    @NotBlank
    @Size(max = 100)
    @Column(name = "session_id", nullable = false, unique = true, length = 100)
    private String sessionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 5)
    @Builder.Default
    private ChatLanguage language = ChatLanguage.FR;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    // Set when status changes to CLOSED
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        this.startedAt = LocalDateTime.now();
    }
}