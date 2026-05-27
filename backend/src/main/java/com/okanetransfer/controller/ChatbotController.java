package com.okanetransfer.controller;

import com.okanetransfer.dto.request.SendMessageRequest;
import com.okanetransfer.dto.request.StartConversationRequest;
import com.okanetransfer.dto.response.SendMessageResponse;
import com.okanetransfer.dto.response.StartConversationResponse;
import com.okanetransfer.entity.ChatbotConversation;
import com.okanetransfer.entity.ChatbotMessage;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Chatbot", description = "AI-powered customer support chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/start")
    @Operation(summary = "Start a new chatbot conversation")
    public ResponseEntity<StartConversationResponse> start(
            @RequestBody StartConversationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(
                chatbotService.startConversation(userId, request));
    }

    @PostMapping("/message")
    @Operation(summary = "Send a message to the chatbot")
    public ResponseEntity<SendMessageResponse> message(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(chatbotService.sendMessage(request, userId));
    }

    @GetMapping("/{sessionId}/messages")
    @Operation(summary = "Get all messages in a conversation")
    public ResponseEntity<List<ChatbotMessage>> getMessages(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(chatbotService.getMessages(sessionId));
    }

    @PatchMapping("/{sessionId}/close")
    @Operation(summary = "Close a conversation")
    public ResponseEntity<Void> close(@PathVariable String sessionId) {
        chatbotService.closeConversation(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/escalated")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all escalated conversations — Admin only")
    public ResponseEntity<List<ChatbotConversation>> getEscalated() {
        return ResponseEntity.ok(chatbotService.getEscalated());
    }

    // ── Helper ─────────────────────────────────────────
    // Returns null for anonymous users (chatbot is public)
    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByEmail(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}