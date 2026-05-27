package com.okanetransfer.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartConversationResponse {
    private Long   conversationId;
    private String sessionId;
    private String welcomeMessage;
}