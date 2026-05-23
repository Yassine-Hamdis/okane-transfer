package com.okanetransfer.repository;

import com.okanetransfer.entity.ChatbotConversation;
import com.okanetransfer.entity.enums.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversation, Long> {
    Optional<ChatbotConversation> findBySessionId(String sessionId);
    List<ChatbotConversation> findAllByUserId(Long userId);
    List<ChatbotConversation> findAllByStatus(ConversationStatus status);
}