package com.okanetransfer.repository;

import com.okanetransfer.entity.ChatbotMessage;
import com.okanetransfer.entity.enums.MessageIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {
    List<ChatbotMessage> findAllByConversationIdOrderBySentAtAsc(Long conversationId);
    List<ChatbotMessage> findAllByIntent(MessageIntent intent);
}