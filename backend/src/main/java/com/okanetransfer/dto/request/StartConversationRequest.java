package com.okanetransfer.dto.request;

import com.okanetransfer.entity.enums.ChatLanguage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartConversationRequest {
    private ChatLanguage language = ChatLanguage.FR;
}